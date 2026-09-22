package ca.cornalix.reporting.report;

import ca.cornalix.reporting.report.dto.ReportResponse;
import ca.cornalix.reporting.security.TenantClaims;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Meme garde-fou tenant que les 3 autres services Cornalix. Le jeton brut
 * a retransmettre aux services en amont est reconstruit depuis le Jwt
 * deja resolu ("Bearer " + jwt.getTokenValue()), meme choix que
 * cornalix-ms-scoring (SCRUM-14) -- evite un conflit avec le filtre de
 * securite si on lisait un @RequestHeader Authorization separe.
 */
@RestController
@RequestMapping("/api/v1/reporting/organizations/{organizationId}/report")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping
    public ReportResponse getReport(@PathVariable UUID organizationId, @AuthenticationPrincipal Jwt jwt) {
        TenantClaims.from(jwt).assertAccessTo(organizationId);
        return service.buildReport(organizationId, "Bearer " + jwt.getTokenValue());
    }
}
