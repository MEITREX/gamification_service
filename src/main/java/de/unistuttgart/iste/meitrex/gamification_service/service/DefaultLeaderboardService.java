package de.unistuttgart.iste.meitrex.gamification_service.service;

import java.time.*;
import java.util.*;

import jakarta.transaction.*;

import org.springframework.stereotype.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.beans.factory.annotation.*;

import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.*;
import de.unistuttgart.iste.meitrex.gamification_service.time.*;
import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@Component
@Transactional
@RestController
class DefaultLeaderboardService implements ILeaderboardService {

    private static final String ERR_MSG_ILLEGAL_ATTEMPT_OF_FUTURE_LEADERBOARD_CREATION
            = "Can not create a new leader board starting in the future.";


    private static void assureUpdateCourseLeaderboardPreconditionsAreMet(CourseEntity courseEntity, LeaderboardEntity leaderboardEntity, LocalDate now, Period period)  {
        Objects.requireNonNull(courseEntity);
        Objects.requireNonNull(leaderboardEntity);
        Objects.requireNonNull(now);
        Objects.requireNonNull(period);
        final LocalDate startDate = leaderboardEntity.getStartDate();
        if(now.isBefore(startDate)) {
            throw new IllegalArgumentException(ERR_MSG_ILLEGAL_ATTEMPT_OF_FUTURE_LEADERBOARD_CREATION);
        }
    }

    private static boolean isDayInRange(LocalDate day, LocalDate beginDate, LocalDate endDate) {
        return !day.isBefore(beginDate)  && day.isBefore(endDate);
    }

    private static LeaderboardEntity instantiateLeaderboard(
            CourseEntity courseEntity,
            LocalDate startDate,
            Period period
    ) {
        // Initializes attributes
        LeaderboardEntity newLeaderboard = new LeaderboardEntity();
        newLeaderboard.setTitle(String.format("%s - %s - %s", courseEntity.getTitle(), period, startDate));
        newLeaderboard.setStartDate(startDate);
        newLeaderboard.setPeriod(period);
        newLeaderboard.setCourse(courseEntity);
        // Updates references
        courseEntity.getLeaderboardEntityList().add(newLeaderboard);
        newLeaderboard.setCourse(courseEntity);
        return newLeaderboard;
    }


    // Repositories

    private final ILeaderboardRepository leaderboardRepository;

    private final ICourseRepository courseRepository;


    //Mapping

    private final LeaderboardMapper leaderboardMapper;

    private final int dtoRecursionDepth;


    private final IPeriodCalculator periodCalculator;

    private final ITimeService timeService;

    public DefaultLeaderboardService(
            @Autowired ILeaderboardRepository leaderboardRepository,
            @Autowired ICourseRepository courseRepository,
            @Autowired IPeriodCalculator periodCalculator,
            @Autowired ITimeService timeService,
            @Autowired LeaderboardMapper leaderboardMapper,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.service.dtoRecursionDepth: 3}")
            int dtoRecursionDepth
    ) {
        this.leaderboardRepository = Objects.requireNonNull(leaderboardRepository);
        this.courseRepository = Objects.requireNonNull(courseRepository);
        this.periodCalculator = Objects.requireNonNull(periodCalculator);
        this.timeService = Objects.requireNonNull(timeService);
        this.leaderboardMapper = Objects.requireNonNull(leaderboardMapper);
        this.dtoRecursionDepth = dtoRecursionDepth;
    }

    // Interface implementation

    @Override
    public List<Leaderboard> find(UUID courseID, LocalDate date, Period period) {
        return this.leaderboardRepository.findByCourseIdAndDateAfterAndPeriod(courseID, date, period)
                .stream()
                .map(leaderboard -> this.leaderboardMapper.toDTO(leaderboard, dtoRecursionDepth))
                .toList();
    }


    // Scheduled logic for leaderboard creation and maintenance.

    @Transactional
    @Scheduled(cron = "0 0 0 * * MON")
    public void runWeeklyLeaderboardUpdate() {
        this.updateAllCourseLeaderboards(Period.WEEKLY);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 1 * *")
    public void runMonthlyLeaderboardUpdate() {
        this.updateAllCourseLeaderboards(Period.MONTHLY);
    }

    @GetMapping("monthly_all")
    public List<Leaderboard> findAllMonthly() {
        return findAll(Period.MONTHLY);
    }

    @GetMapping("weekly_all")
    public List<Leaderboard> findAllWeekly() {
        return findAll(Period.WEEKLY);
    }

    @Autowired
    private LeaderboardMapper mapper;

    public List<Leaderboard> findAll(Period period) {
        List<LeaderboardEntity> list = new ArrayList<>();
        this.courseRepository
                .findAll()
                .forEach(courseEntity -> {
                    this.leaderboardRepository
                            .findFirstByCourseAndPeriodOrderByStartDateDesc(courseEntity, period)
                            .stream()
                            .findFirst().ifPresent(list::add);
                });
        return list.stream()
                .map(leaderboardEntity -> mapper.toDTO(leaderboardEntity, 1))
                .toList();
    }

    private void updateAllCourseLeaderboards(Period period) {
        LocalDate localDate = this
                .timeService
                .toDate();
        this.courseRepository
                .findAll()
                .forEach(courseEntity -> this.updateCourseLeaderboard(courseEntity, localDate, period));
    }

    private void updateCourseLeaderboard(CourseEntity courseEntity, LocalDate now, Period period){
        this.leaderboardRepository
                .findFirstByCourseAndPeriodOrderByStartDateDesc(courseEntity, period)
                .stream()
                .findFirst()
                .ifPresent(leaderboard -> this.updateCourseLeaderboard(courseEntity, leaderboard, now, period));
    }

    private void updateCourseLeaderboard(CourseEntity courseEntity, LeaderboardEntity leaderboardEntity,  LocalDate now, Period period) {
        assureUpdateCourseLeaderboardPreconditionsAreMet(courseEntity, leaderboardEntity, now, period);
        final LocalDate curStartDate = leaderboardEntity.getStartDate();
        final LocalDate nextStartDate = this.periodCalculator.calcSucStartDate(curStartDate, period);
        if(isDayInRange(now, curStartDate, nextStartDate)) {
            return;
        }
        LeaderboardEntity newLeaderboard = instantiateLeaderboard(courseEntity, nextStartDate, period);
        newLeaderboard = this.leaderboardRepository.save(newLeaderboard);
        updateCourseLeaderboard(courseEntity, newLeaderboard, now, period);
    }
}
