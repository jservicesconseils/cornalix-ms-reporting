package ca.cornalix.reporting.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

/**
 * Appelle cornalix-ms-scoring en HTTP (SCRUM-7/8/9 : microservices
 * separes des le MVP). Le jeton de l'appelant est transmis tel quel.
 */
@Component
public class ScoringClient {

    private static final String SERVICE_NAME = "cornalix-ms-scoring";

    private final RestClient restClient;

    public ScoringClient(RestClient.Builder builder, @Value("${cornalix.scoring.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public ScoreSummary fetchScore(String authorizationHeader, UUID organizationId) {
        try {
            return restClient.get()
                    .uri("/api/v1/scoring/organizations/{organizationId}/score", organizationId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(ScoreSummary.class);
        } catch (RestClientException e) {
            throw new UpstreamServiceUnavailableException(SERVICE_NAME, e);
        }
    }
}
