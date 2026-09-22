package ca.cornalix.reporting.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import java.util.UUID;

/**
 * Miroir de ScoreResponse tel que renvoye par
 * GET /api/v1/scoring/organizations/{id}/score sur cornalix-ms-scoring
 * (SCRUM-14/15). {@code overallScore} et les entrees des deux maps sont
 * {@code null}/vides quand l'organisation n'a pas encore de reponse
 * exploitable -- distinct d'un score de 0.0.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ScoreSummary(
        UUID organizationId,
        Double overallScore,
        Map<String, Double> scoreByFunction,
        Map<Integer, Double> scoreByCisControl
) {
}
