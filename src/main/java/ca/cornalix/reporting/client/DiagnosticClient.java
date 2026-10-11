package ca.cornalix.reporting.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Appelle cornalix-ms-diagnostic en HTTP (SCRUM-7/8/9 : microservices
 * separes des le MVP). Le jeton de l'appelant est transmis tel quel.
 *
 * SCRUM-49 : timeout explicite + retry avec backoff et circuit breaker
 * Resilience4j (config resilience4j.* dans application.yml, instance
 * "diagnosticClient") -- une panne/lenteur transitoire de cornalix-ms-
 * diagnostic ne doit pas immediatement echouer, ni marteler un service
 * deja en panne. Les annotations ne voient jamais RestClientException
 * directement (catchee plus bas), seulement UpstreamServiceUnavailableException.
 */
@Component
public class DiagnosticClient {

    private static final String SERVICE_NAME = "cornalix-ms-diagnostic";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    private final RestClient restClient;

    public DiagnosticClient(RestClient.Builder builder, @Value("${cornalix.diagnostic.base-url}") String baseUrl) {
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(
                ClientHttpRequestFactorySettings.DEFAULTS
                        .withConnectTimeout(CONNECT_TIMEOUT)
                        .withReadTimeout(READ_TIMEOUT));
        this.restClient = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @Retry(name = "diagnosticClient")
    @CircuitBreaker(name = "diagnosticClient")
    public List<QuestionSummary> fetchQuestions(String authorizationHeader) {
        try {
            return restClient.get()
                    .uri("/api/v1/diagnostics/questions")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<QuestionSummary>>() { });
        } catch (RestClientException e) {
            throw new UpstreamServiceUnavailableException(SERVICE_NAME, e);
        }
    }

    @Retry(name = "diagnosticClient")
    @CircuitBreaker(name = "diagnosticClient")
    public List<AnswerSummary> fetchAnswers(String authorizationHeader, UUID organizationId) {
        try {
            return restClient.get()
                    .uri("/api/v1/diagnostics/organizations/{organizationId}/answers", organizationId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<AnswerSummary>>() { });
        } catch (RestClientException e) {
            throw new UpstreamServiceUnavailableException(SERVICE_NAME, e);
        }
    }
}
