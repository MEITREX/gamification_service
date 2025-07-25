package de.unistuttgart.iste.meitrex.gamification_service.config;

import de.unistuttgart.iste.meitrex.course_service.client.CourseServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class CourseServiceConfiguration {

    @Value("${course_service.url}")
    private String courseServiceUrl;

    @Bean
    public CourseServiceClient courseServiceClient() {
        final WebClient webClient = WebClient.builder().baseUrl(courseServiceUrl).build();
        log.info("{};{}", webClient, courseServiceUrl);
        final GraphQlClient graphQlClient = HttpGraphQlClient.builder(webClient).build();
        return new CourseServiceClient(graphQlClient);
    }
}
