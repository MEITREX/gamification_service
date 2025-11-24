package de.unistuttgart.iste.meitrex.gamification_service.config;

import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class ContentServiceConfiguration {

    @Value("${content_service.url}")
    private String contentServiceUrl;

    @Bean
    public ContentServiceClient contentServiceClient() {
        final int bufferSize = (int)DataSize.ofMegabytes(4).toBytes();
        final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(bufferSize))
                .build();
        final WebClient webClient = WebClient.builder()
                .exchangeStrategies(exchangeStrategies)
                .baseUrl(contentServiceUrl)
                .build();
        log.info("{};{}", webClient, contentServiceUrl);

        final GraphQlClient graphQlClient = HttpGraphQlClient.builder(webClient).build();
        return new ContentServiceClient(graphQlClient);
    }
}