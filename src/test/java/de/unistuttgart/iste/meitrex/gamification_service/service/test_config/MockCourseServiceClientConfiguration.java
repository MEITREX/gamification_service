package de.unistuttgart.iste.meitrex.gamification_service.service.test_config;

import de.unistuttgart.iste.meitrex.course_service.client.CourseServiceClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MockCourseServiceClientConfiguration {
    @Primary
    @Bean
    public CourseServiceClient getTestContentServiceClient() {
        return Mockito.mock(CourseServiceClient.class);
    }
}
