package ca.cornalix.reporting.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.UUID;

/**
 * Appelle cornalix-ms-scoring en HTTP (SCRUM-7/8/9 : microservices
 * separes des le MVP). Le jeton de l'appelant est transmis tel quel.
 *
 * SCRUM-49 : meme traitement de resilience que DiagnosticClient, instance
 * Resilience4j separee ("scoringClient") pour qu'une panne de scoring et
 * une panne de diagnostic n'ouvrent pas le meme circuit -- ce sont deux
 * dependances independantes.
 */
@Component
public class ScoringClient {

    private static final String SERVICE_NAME = "cornalix-ms-scoring";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    private final RestClient restClient;

    public ScoringClient(RestClient.Builder builder, @Value("${cornalix.scoring.base-url}") String baseUrl) {
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(
                ClientHttpRequestFactorySettings.DEFAULTS
                        .withConnectTimeout(CONNECT_TIMEOUT)
                        .withReadTimeout(READ_TIMEOUT));
        this.restClient = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @Retry(name = "scoringClient")
    @CircuitBreaker(name = "scoringClient")
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
