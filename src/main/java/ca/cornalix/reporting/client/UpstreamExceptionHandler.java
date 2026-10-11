package ca.cornalix.reporting.client;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class UpstreamExceptionHandler {

    @ExceptionHandler(UpstreamServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleUpstreamUnavailable(UpstreamServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorBody(ex.getMessage()));
    }

    // SCRUM-49 : le circuit breaker (diagnosticClient ou scoringClient)
    // rejette l'appel sans meme invoquer le client HTTP -- meme reponse
    // 502 que UpstreamServiceUnavailableException.
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<Map<String, Object>> handleCircuitOpen(CallNotPermittedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorBody(
                "Un service en amont est temporairement marque indisponible (circuit ouvert), reessayez dans quelques instants"));
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("message", message);
        return body;
    }
}
