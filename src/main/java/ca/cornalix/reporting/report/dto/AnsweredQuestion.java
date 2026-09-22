package ca.cornalix.reporting.report.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Une question du catalogue jointe a la reponse de l'organisation (s'il y en a une). */
public record AnsweredQuestion(
        UUID questionId,
        int cisControl,
        String cisSafeguard,
        String nistFunction,
        Map<String, String> translations,
        String value,
        Instant answeredAt
) {
}
