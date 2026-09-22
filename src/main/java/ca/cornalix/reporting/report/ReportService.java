package ca.cornalix.reporting.report;

import ca.cornalix.reporting.client.AnswerSummary;
import ca.cornalix.reporting.client.DiagnosticClient;
import ca.cornalix.reporting.client.QuestionSummary;
import ca.cornalix.reporting.client.ScoreSummary;
import ca.cornalix.reporting.client.ScoringClient;
import ca.cornalix.reporting.report.dto.AnsweredQuestion;
import ca.cornalix.reporting.report.dto.ReportResponse;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SCRUM-16 : combine le score (cornalix-ms-scoring) et le detail des
 * reponses (cornalix-ms-diagnostic) d'une organisation en un seul rapport
 * de lecture. Rien n'est stocke ni mis en cache -- recalcule/rejoint a
 * chaque appel, comme cornalix-ms-scoring.
 */
@Service
public class ReportService {

    private final DiagnosticClient diagnosticClient;
    private final ScoringClient scoringClient;

    public ReportService(DiagnosticClient diagnosticClient, ScoringClient scoringClient) {
        this.diagnosticClient = diagnosticClient;
        this.scoringClient = scoringClient;
    }

    public ReportResponse buildReport(UUID organizationId, String authorizationHeader) {
        List<QuestionSummary> questions = diagnosticClient.fetchQuestions(authorizationHeader);
        List<AnswerSummary> answerSummaries = diagnosticClient.fetchAnswers(authorizationHeader, organizationId);
        ScoreSummary score = scoringClient.fetchScore(authorizationHeader, organizationId);

        Map<UUID, QuestionSummary> questionsById = new LinkedHashMap<>();
        questions.forEach(q -> questionsById.put(q.id(), q));

        List<AnsweredQuestion> answers = answerSummaries.stream()
                .map(answer -> toAnsweredQuestion(answer, questionsById.get(answer.questionId())))
                .filter(java.util.Objects::nonNull) // reponse a une question qui n'existe plus dans le catalogue
                .toList();

        return new ReportResponse(
                organizationId,
                score.overallScore(),
                score.scoreByFunction(),
                score.scoreByCisControl(),
                answers
        );
    }

    private AnsweredQuestion toAnsweredQuestion(AnswerSummary answer, QuestionSummary question) {
        if (question == null) {
            return null;
        }
        return new AnsweredQuestion(
                question.id(),
                question.cisControl(),
                question.cisSafeguard(),
                question.nistFunction(),
                question.translations(),
                answer.value(),
                answer.answeredAt()
        );
    }
}
