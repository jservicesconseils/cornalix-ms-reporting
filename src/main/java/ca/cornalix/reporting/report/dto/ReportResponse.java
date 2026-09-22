package ca.cornalix.reporting.report.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Rapport de base (SCRUM-16) : le score courant (memes champs que
 * ScoreResponse de cornalix-ms-scoring) accompagne du detail des questions
 * auxquelles l'organisation a repondu -- pas les questions non repondues,
 * meme logique de lecture que le calcul du score (SCRUM-14).
 */
public record ReportResponse(
        UUID organizationId,
        Double overallScore,
        Map<String, Double> scoreByFunction,
        Map<Integer, Double> scoreByCisControl,
        List<AnsweredQuestion> answers
) {
}
