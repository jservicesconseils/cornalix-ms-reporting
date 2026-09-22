cornalix-ms-reporting — service de rapport

Rôle dans l'architecture
Troisième microservice **métier** de Cornalix (voir la proposition d'architecture, §5 : cycle de valeur). Il combine le score de maturité (`cornalix-ms-scoring`) et le détail des réponses au questionnaire (`cornalix-ms-diagnostic`) en un rapport de lecture simple pour une organisation. Il ne possède aucune donnée lui-même : il consomme les deux autres services en HTTP à chaque appel.

Ce qu'il fait (dans son périmètre, MVP — « rapport de base »)

Exposer un rapport combinant le score courant (par fonction NIST CSF 2.0 et par contrôle CIS Controls v8) et le détail des questions auxquelles l'organisation a répondu, avec le texte des questions (traductions complètes, pas de sélection de langue pour cette première version).
Rien n'est stocké ni mis en cache — recalculé/rejoint à chaque appel.

Ce qu'il ne fait PAS (hors périmètre, volontairement — grandira dans ce même service plus tard, pas un nouvel epic)

Génération de rapports PDF/Word, gabarits Loi 25 (ÉFVP, registre d'incidents).
Registre de consentement, demandes d'accès/portabilité.
Export/partage de rapports.
Gérer l'authentification, les organisations, le catalogue de questions, les réponses ou le calcul du score — les trois autres microservices Cornalix.

Sécurité

Comme tous les microservices Cornalix, ce service ne gère aucun mot de passe : il valide les jetons JWT émis par le même User Pool Amazon Cognito que les autres services (`SecurityConfig`, JWKS). Toute route (hors `/actuator/health` et `/actuator/info`) exige un jeton valide, et l'accès au rapport d'une organisation est vérifié contre le `tenant_id`/`tenant_scope` du jeton — même garde-fou que les 3 autres services. Le jeton de l'appelant est transmis tel quel à `cornalix-ms-diagnostic` et `cornalix-ms-scoring` lors des appels HTTP sous-jacents.

Suivi Jira

Epic : [SCRUM-9 — Cornalix — Conformité & rapports (MVP, rapport de base)](https://jservicesconseils.atlassian.net/browse/SCRUM-9), projet SCRUM.

Développer en local

```
mvn spring-boot:run
```

Démarre sur `http://localhost:8084` (8080 = core-api, 8081 = identity, 8082 = diagnostic, 8083 = scoring — les deux derniers doivent tourner en parallèle pour que ce service puisse construire un rapport). Nécessite un jeton Cognito valide (même pool que les autres services) pour toute route protégée.
