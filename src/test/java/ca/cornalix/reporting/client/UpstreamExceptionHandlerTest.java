package ca.cornalix.reporting.client;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpstreamExceptionHandlerTest {

    private final UpstreamExceptionHandler handler = new UpstreamExceptionHandler();

    @Test
    void serviceEnAmontIndisponible_renvoie502() {
        UpstreamServiceUnavailableException ex = new UpstreamServiceUnavailableException("cornalix-ms-diagnostic", new RuntimeException());

        ResponseEntity<Map<String, Object>> response = handler.handleUpstreamUnavailable(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertTrue(response.getBody().get("message").toString().contains("cornalix-ms-diagnostic"));
    }

    // SCRUM-49 : quand le circuit est ouvert (echecs recents trop
    // nombreux), Resilience4j rejette l'appel avant meme d'invoquer le
    // client HTTP -- meme reponse 502 que UpstreamServiceUnavailableException.
    @Test
    void circuitOuvert_renvoie502() {
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("diagnosticClient");
        CallNotPermittedException ex = CallNotPermittedException.createCallNotPermittedException(circuitBreaker);

        ResponseEntity<Map<String, Object>> response = handler.handleCircuitOpen(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertTrue(response.getBody().get("message").toString().contains("circuit ouvert"));
    }
}
