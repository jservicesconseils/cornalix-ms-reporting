package ca.cornalix.reporting.report;

import ca.cornalix.reporting.client.UpstreamServiceUnavailableException;
import ca.cornalix.reporting.report.dto.ReportResponse;
import ca.cornalix.reporting.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import(SecurityConfig.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void lireLeRapport_avecTenantIdCorrespondant_renvoie200() throws Exception {
        UUID orgId = UUID.randomUUID();
        when(reportService.buildReport(eq(orgId), anyString()))
                .thenReturn(new ReportResponse(orgId, 0.5, Map.of("IDENTIFY", 0.5), Map.of(1, 0.5), List.of()));

        mockMvc.perform(get("/api/v1/reporting/organizations/{orgId}/report", orgId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", orgId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallScore").value(0.5));
    }

    @Test
    void lireLeRapport_avecTenantIdDifferent_renvoie403() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/reporting/organizations/{orgId}/report", orgId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void lireLeRapport_sansTenantId_renvoie403() throws Exception {
        UUID orgId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/reporting/organizations/{orgId}/report", orgId)
                        .with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void lireLeRapport_sansJeton_renvoie401() throws Exception {
        UUID orgId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/reporting/organizations/{orgId}/report", orgId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void lireLeRapport_serviceEnAmontIndisponible_renvoie502() throws Exception {
        UUID orgId = UUID.randomUUID();
        when(reportService.buildReport(eq(orgId), anyString()))
                .thenThrow(new UpstreamServiceUnavailableException("cornalix-ms-scoring", new RuntimeException()));

        mockMvc.perform(get("/api/v1/reporting/organizations/{orgId}/report", orgId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", orgId.toString()))))
                .andExpect(status().isBadGateway());
    }
}
