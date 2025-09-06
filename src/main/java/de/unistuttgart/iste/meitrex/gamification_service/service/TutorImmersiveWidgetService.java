package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.common.ollama.OllamaClient;
import de.unistuttgart.iste.meitrex.common.ollama.OllamaRequest;
import de.unistuttgart.iste.meitrex.common.ollama.OllamaResponse;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.gamification_service.config.AdaptivityConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.TutorImmersiveSpeechEmbeddable;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserCourseDataEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TutorImmersiveWidgetService {
    private final OllamaClient ollamaClient;
    private final IUserCreator userCreator;
    private final AdaptivityConfiguration adaptivityConfiguration;
    private final ContentServiceClient contentService;

    public String getSpeechContent(final LoggedInUser loggedInUser, final UUID courseId) {
        UserEntity userEntity = userCreator.fetchOrCreate(loggedInUser.getId());
        Optional<UserCourseDataEntity> courseData = userEntity.getCourseData(courseId);
        if(courseData.isEmpty()) {
            log.warn("getSpeechContent(): Course data for course {} not found for user {}",
                    courseId, loggedInUser.getId());
            return "Hey! Welcome to the course. Let's learn together!";
        }

        TutorImmersiveSpeechEmbeddable tutorSpeech = courseData.get().getTutorImmersiveSpeech();
        try {
            String userActivityString = getUserRecentActivitiesString(loggedInUser.getId(), courseId);

            // if we don't have a speech yet or if the user's recent activities have changed, generate a new speech
            // for the tutor, otherwise return what we already have
            if(tutorSpeech == null || !tutorSpeech.getRecentActivitiesString().equals(userActivityString)) {
                if(tutorSpeech == null)
                    tutorSpeech = new TutorImmersiveSpeechEmbeddable();

                tutorSpeech.setRecentActivitiesString(userActivityString);
                tutorSpeech.setTutorSpeechContent(generateSpeechContent(loggedInUser, userActivityString));
            }

            return tutorSpeech.getTutorSpeechContent();
        } catch (ContentServiceConnectionException ex) {
            log.error("Error while generating immersive tutor speech.", ex);
            return adaptivityConfiguration.getImmersiveTutorSpeechGeneric();
        }
    }

    private String generateSpeechContent(final LoggedInUser user, final String userActivityString) {
        try {
            final Map<String, String> placeholders = Map.of(
                    "tutor_name", "Dino", // TODO: Insert actual tutor name
                    "user_info", getUserInfoString(user),
                    "user_activity", userActivityString
            );
            final String prompt = getPrompt(placeholders);

            final OllamaResponse res = ollamaClient.queryLLM(new OllamaRequest(
                    adaptivityConfiguration.getImmersiveTutorOllamaModel(),
                    prompt));
            return res.getResponse();
        } catch (final IOException | InterruptedException ex) {
            log.error("Error while generating immersive tutor speech.", ex);
            return "";
        }
    }

    private String getUserInfoString(final LoggedInUser user) {
        return "Username: " + user.getUserName() + "\n" +
                "First name: " + user.getFirstName() + "\n";
    }

    private String getUserRecentActivitiesString(final UUID userId,
                                                 final UUID courseId) throws ContentServiceConnectionException {
        final List<Content> courseContents = contentService.queryContentsOfCourse(userId, courseId);

        final LocalDate today = OffsetDateTime.now().toLocalDate();
        final LocalDate yesterday = OffsetDateTime.now().minusDays(1).toLocalDate();

        final Map<LocalDate, List<Content>> contentsWorkedOnRecently = groupDateTimeItemsIntoDays(
                courseContents.stream()
                        .filter(c -> c.getUserProgressData().getLastLearnDate() != null)
                        .toList(),
                List.of(yesterday, today),
                c -> c.getUserProgressData().getLastLearnDate());

        final StringBuilder sb = new StringBuilder();
        sb.append("Items completed today:\n");
        sb.append(generateContentListString(contentsWorkedOnRecently.get(today)));

        sb.append("\n\n");
        sb.append("Items completed yesterday:\n");
        sb.append(generateContentListString(contentsWorkedOnRecently.get(yesterday)));

        log.info(sb.toString());

        return sb.toString();
    }

    private String generateContentListString(final List<Content> contents) {
        if(contents.isEmpty())
            return "No items completed.";

        final StringBuilder sb = new StringBuilder();

        boolean first = true;
        for(Content content : contents) {
            if(!first) {
                sb.append("\n");
            }

            sb.append("* ");

            if(content instanceof MediaContent) {
                sb.append("Lecture: \"").append(content.getMetadata().getName()).append("\"");
            } else if(content instanceof Assessment assessment) {
                sb.append("Assessment: \"").append(content.getMetadata().getName()).append("\"\n");

                if(!assessment.getItems().isEmpty()) {
                    sb.append("  - Topics: ");
                    Stream<Skill> skills = assessment.getItems().stream()
                            .flatMap(it -> it.getAssociatedSkills().stream())
                            .distinct();
                    sb.append(skills
                            .map(sk -> sk.getSkillCategory() + " - " + sk.getSkillName())
                            .collect(Collectors.joining(", ")));
                    sb.append("\n");
                }

                Optional<ProgressLogItem> latestCompletion = assessment.getUserProgressData().getLog().stream()
                        .max(Comparator.comparing(ProgressLogItem::getTimestamp));
                if(latestCompletion.isPresent()) {
                    sb.append("  - Correctness: ");
                    sb.append(String.format("%.02f", latestCompletion.get().getCorrectness() * 100)).append("%");
                }
            }

            first = false;
        }

        return sb.toString();
    }

    private String getPrompt(final Map<String, String> placeholders) {
        String template = getPromptTemplate();
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return template;
    }

    private String getPromptTemplate() {
        try(InputStream is = this.getClass().getResourceAsStream("/prompt_templates/tutorImmersiveWidget.txt")) {
            if(is == null)
                throw new RuntimeException(
                        "prompt_templates/tutorImmersiveWidget.txt could not be found in resources directory.");

            // read input stream as string
            return new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));
        } catch(IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Helper method which, given a list of items, a mapping function mapping an item to an OffsetDateTime, and a list
     * of LocalDates, sorts the items into a map based on which day of the LocalDates the mapped OffsetDateTimes match.
     */
    private static <T> Map<LocalDate, List<T>> groupDateTimeItemsIntoDays(
            final List<T> items,
            final List<LocalDate> days,
            final Function<T, OffsetDateTime> function) {

        final Map<LocalDate, List<T>> result = new HashMap<>();

        for(LocalDate day : days) {
            result.put(day, new ArrayList<>());
        }

        for(final T item : items) {
            final LocalDate dateOfItem = function.apply(item).toLocalDate();
            if(result.containsKey(dateOfItem))
                result.get(dateOfItem).add(item);
        }

        return result;
    }
}
