package de.unistuttgart.iste.meitrex.gamification_service.config;

import de.unistuttgart.iste.meitrex.course_service.client.CourseServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;


@Slf4j
public class UserServiceConfiguration {

    @Value("${user_service.url}")
    private String userServiceUrl;

    @Bean
    public CourseServiceClient courseServiceClient() {
        final WebClient webClient = WebClient.builder().baseUrl(userServiceUrl).build();
        log.info("{};{}", webClient, userServiceUrl);
        final GraphQlClient graphQlClient = HttpGraphQlClient.builder(webClient).build();
        return new CourseServiceClient(graphQlClient);
    }
}
