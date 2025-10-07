package de.unistuttgart.iste.meitrex.gamification_service.keycloak;


import java.util.*;

import de.unistuttgart.iste.meitrex.gamification_service.aspects.logging.Loggable;
import de.unistuttgart.iste.meitrex.gamification_service.aspects.resiliency.Retryable;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.reactive.function.*;
import org.springframework.web.reactive.function.client.*;
import org.springframework.beans.factory.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;


@Component
public class DefaultKeycloakClient implements IKeycloakClient {

    private static Map<String, List<String>> extractAttributeMap(JsonNode node) {
        if(Objects.isNull(node)) {
            return new HashMap<>();
        }
        final Map<String, List<String>> result = new HashMap<>();
        final JsonNode attributesNode = node.path("attributes");
        if (attributesNode.isObject()) {
            final Iterator<String> attributeIterator = attributesNode.fieldNames();
            while(attributeIterator.hasNext()) {
                final String attributeName = attributeIterator.next();
                final JsonNode valueNode = attributesNode.get(attributeName);
                final List<String> values = new ArrayList<>();
                if (valueNode.isArray()) {
                    valueNode.forEach(v -> values.add(v.asText()));
                }
                result.put(attributeName, values);
            }
        }
        return result;
    }

    private final String realm;

    private final String clientId;

    private final String clientSecret;

    private final WebClient webClient;


    public DefaultKeycloakClient(
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.client-id}") String clientId,
            @Value("${keycloak.client-secret}") String clientSecret,
            @Autowired @Qualifier("keycloakServiceClient") WebClient webClient
    ) {
        this.realm = Objects.requireNonNull(realm);
        this.clientId = Objects.requireNonNull(clientId);
        this.clientSecret = Objects.requireNonNull(clientSecret);
        this.webClient = Objects.requireNonNull(webClient);
    }

    @Override
    @Loggable(
            inLogLevel = Loggable.LogLevel.INFO,
            exitLogLevel = Loggable.LogLevel.DEBUG,
            exceptionLogLevel = Loggable.LogLevel.DEBUG,
            logExecutionTime = false
    )
    @Retryable(maxRetries = 3, backoffMillis = 2000, retryOn = {
            WebClientRequestException.class,
            io.netty.handler.timeout.ReadTimeoutException.class,
            io.netty.handler.timeout.WriteTimeoutException.class
    })
    public List<String> getValues(UUID userId, String attrName) {
        return Collections.unmodifiableList(readMutableAttributeMap(userId).getOrDefault(attrName, List.of()));
    }


    @Override
    @Loggable(
            inLogLevel = Loggable.LogLevel.INFO,
            exitLogLevel = Loggable.LogLevel.DEBUG,
            exceptionLogLevel = Loggable.LogLevel.DEBUG,
            logExecutionTime = false
    )
    @Retryable(maxRetries = 3, backoffMillis = 2000, retryOn = {
            WebClientRequestException.class,
            io.netty.handler.timeout.ReadTimeoutException.class,
            io.netty.handler.timeout.WriteTimeoutException.class
    })
    public void setValues(UUID userId, String attrName, List<String> values) {
        if(Objects.isNull(values) || values.isEmpty()) {
            removeValues(userId, attrName);
        }
        else {
            upsertValues(userId, attrName, values);
        }
    }

    private void upsertValues(UUID userId, String attrName, List<String> values) {
        final Map<String, List<String>> attrbMap = readMutableAttributeMap(userId);
        attrbMap.put(attrName, values);
        saveAttributeMap(userId, attrbMap);
    }

    private void removeValues(UUID userId, String attrName) {
        final Map<String, List<String>> attrbMap = readMutableAttributeMap(userId);
        attrbMap.remove(attrName);
        saveAttributeMap(userId, attrbMap);
    }

    private void saveAttributeMap(UUID userId, Map<String, List<String>> valueMap) {
        final String token = getAccessToken();
        Map<String, Object> payload = new HashMap<>();
        payload.put("attributes", valueMap);
        webClient.put()
                .uri("/admin/realms/" + realm + "/users/{id}", userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .block();
    }


    private Map<String, List<String>> readMutableAttributeMap(UUID userId) {
        final String token = getAccessToken();
        final JsonNode node = webClient.get()
                .uri("/admin/realms/" + this.realm + "/users/" + userId)
                .headers(headers -> headers.setBearerAuth(token)) // <-- Correct way
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        return extractAttributeMap(node);
    }

    private String getAccessToken() {
        return webClient.post()
                .uri("/realms/" + realm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(h -> h.setBasicAuth(clientId, clientSecret))
                .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.get("access_token").asText())
                .block();
    }

}
