package ca.cornalix.reporting.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

/**
 * Vue d'une reponse telle que renvoyee par
 * GET /api/v1/diagnostics/organizations/{id}/answers sur
 * cornalix-ms-diagnostic (SCRUM-12). {@code value} reste une String brute
 * plutot qu'un enum local -- evite de faire echouer le rapport si
 * cornalix-ms-diagnostic ajoute une valeur que ce service ne connait pas
 * encore ; elle est simplement affichee telle quelle dans le rapport.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnswerSummary(UUID questionId, String value, Instant answeredAt) {
}
