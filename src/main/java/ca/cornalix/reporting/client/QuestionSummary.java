package ca.cornalix.reporting.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import java.util.UUID;

/**
 * Vue d'une question telle que renvoyee par GET /api/v1/diagnostics/questions
 * sur cornalix-ms-diagnostic (SCRUM-10/11). Contrairement a cornalix-ms-
 * scoring (qui n'a besoin que de nistFunction/cisControl pour calculer),
 * ce service reprend aussi {@code translations} : le rapport doit pouvoir
 * afficher le texte de la question, pas seulement des chiffres.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record QuestionSummary(
        UUID id,
        int cisControl,
        String cisSafeguard,
        String nistFunction,
        Map<String, String> translations
) {
}
