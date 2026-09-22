package ca.cornalix.reporting.report;

import ca.cornalix.reporting.client.AnswerSummary;
import ca.cornalix.reporting.client.DiagnosticClient;
import ca.cornalix.reporting.client.QuestionSummary;
import ca.cornalix.reporting.client.ScoreSummary;
import ca.cornalix.reporting.client.ScoringClient;
import ca.cornalix.reporting.report.dto.ReportResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private DiagnosticClient diagnosticClient;

    @Mock
    private ScoringClient scoringClient;

    private ReportService service() {
        return new ReportService(diagnosticClient, scoringClient);
    }

    @Test
    void buildReport_joinLesReponsesAuCatalogueEtInclutLeScore() {
        ReportService service = service();
        UUID orgId = UUID.randomUUID();
        UUID q1 = UUID.randomUUID();
        Instant answeredAt = Instant.now();

        when(diagnosticClient.fetchQuestions("Bearer token")).thenReturn(List.of(
                new QuestionSummary(q1, 1, "1.1", "IDENTIFY", Map.of("fr", "Avez-vous un inventaire ?"))
        ));
        when(diagnosticClient.fetchAnswers("Bearer token", orgId)).thenReturn(List.of(
                new AnswerSummary(q1, "YES", answeredAt)
        ));
        when(scoringClient.fetchScore("Bearer token", orgId)).thenReturn(
                new ScoreSummary(orgId, 1.0, Map.of("IDENTIFY", 1.0), Map.of(1, 1.0))
        );

        ReportResponse report = service.buildReport(orgId, "Bearer token");

        assertEquals(1.0, report.overallScore());
        assertEquals(1, report.answers().size());
        assertEquals(q1, report.answers().get(0).questionId());
        assertEquals("YES", report.answers().get(0).value());
        assertEquals("Avez-vous un inventaire ?", report.answers().get(0).translations().get("fr"));
        assertEquals(answeredAt, report.answers().get(0).answeredAt());
    }

    @Test
    void buildReport_reponseAUneQuestionInconnue_estExclueDuRapport() {
        ReportService service = service();
        UUID orgId = UUID.randomUUID();
        UUID questionSupprimee = UUID.randomUUID();

        when(diagnosticClient.fetchQuestions("Bearer token")).thenReturn(List.of());
        when(diagnosticClient.fetchAnswers("Bearer token", orgId)).thenReturn(List.of(
                new AnswerSummary(questionSupprimee, "YES", Instant.now())
        ));
        when(scoringClient.fetchScore("Bearer token", orgId)).thenReturn(
                new ScoreSummary(orgId, null, Map.of(), Map.of())
        );

        ReportResponse report = service.buildReport(orgId, "Bearer token");

        assertTrue(report.answers().isEmpty());
    }

    @Test
    void buildReport_aucuneReponse_renvoieUnRapportVideAvecLeScoreDuServiceScoring() {
        ReportService service = service();
        UUID orgId = UUID.randomUUID();

        when(diagnosticClient.fetchQuestions("Bearer token")).thenReturn(List.of());
        when(diagnosticClient.fetchAnswers("Bearer token", orgId)).thenReturn(List.of());
        when(scoringClient.fetchScore("Bearer token", orgId)).thenReturn(
                new ScoreSummary(orgId, null, Map.of(), Map.of())
        );

        ReportResponse report = service.buildReport(orgId, "Bearer token");

        assertTrue(report.answers().isEmpty());
        assertEquals(null, report.overallScore());
    }
}
