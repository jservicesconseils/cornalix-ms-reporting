package ca.cornalix.reporting.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

/**
 * Appelle cornalix-ms-diagnostic en HTTP (SCRUM-7/8/9 : microservices
 * separes des le MVP). Le jeton de l'appelant est transmis tel quel.
 */
@Component
public class DiagnosticClient {

    private static final String SERVICE_NAME = "cornalix-ms-diagnostic";

    private final RestClient restClient;

    public DiagnosticClient(RestClient.Builder builder, @Value("${cornalix.diagnostic.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

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
