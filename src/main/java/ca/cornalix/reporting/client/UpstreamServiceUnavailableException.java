package ca.cornalix.reporting.client;

/**
 * Levee quand l'appel HTTP vers un service en amont (cornalix-ms-diagnostic
 * ou cornalix-ms-scoring) echoue (service injoignable, erreur inattendue).
 * Un seul type d'exception pour les deux services consommes -- les deux
 * pannes se traduisent de la meme facon (502 Bad Gateway), pas de raison
 * de dupliquer une classe par service.
 */
public class UpstreamServiceUnavailableException extends RuntimeException {

    public UpstreamServiceUnavailableException(String serviceName, Throwable cause) {
        super("Impossible de contacter " + serviceName, cause);
    }
}
