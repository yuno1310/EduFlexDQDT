# EduFlex monitoring runbook

## Start and access

Copy `backend/.env.example` to `backend/.env`, use strong local passwords, then run
from the repository root:

```bash
docker compose --env-file backend/.env \
  -f backend/docker-compose.yml \
  -f backend/docker-compose.monitoring.yml up --build -d
```

- Grafana: <http://127.0.0.1:3000> using `GRAFANA_ADMIN_USER` and
  `GRAFANA_ADMIN_PASSWORD` from the untracked environment file.
- Prometheus: <http://127.0.0.1:9090>
- Alertmanager: <http://127.0.0.1:9093>
- RabbitMQ management: <http://127.0.0.1:15672>

Grafana, Prometheus and Alertmanager bind only to loopback. Exporters and the API
management listener are reachable only on the Compose network. Production access
requires authenticated TLS at a reverse proxy or private network boundary.

The Compose overlay creates or updates a dedicated `eduflex_monitor` PostgreSQL role
with `pg_monitor`; its password comes from `POSTGRES_MONITOR_PASSWORD`. The application
database user is not shared with the exporter. The setup job runs for fresh and existing
PostgreSQL volumes.

Run `backend/scripts/verify-monitoring.sh` to validate rules, start the stack and
check every target and provisioned dashboard. Set `CLEANUP_MONITORING=true` only for
a disposable project when its volumes should be removed after verification.

## Dashboard interpretation

**Service Overview** covers the application listener, API traffic and latency, JVM
heap and the Hikari database pool. A healthy management scrape does not replace the
main-listener probe. HTTP 4xx usually indicates client/auth behavior; investigate 5xx.

**Dependencies** covers PostgreSQL, Redis and RabbitMQ. Redis `maxmemory` may be zero
(unlimited); read memory bytes directly in that case. Queue-ready messages represent
waiting embedding work, while unacknowledged messages are currently being processed.

**Learning Operations** shows committed lesson/reward transitions, learner row-lock
time and embedding work. Publish `sent` means the Rabbit client call returned; the
current design has no publisher confirmation or transactional outbox, so it does not
prove durable broker acceptance.

Idle meters can be absent until the first relevant event. Custom bounded outcome
counters are initialized at startup. Never add learner IDs, course IDs, email, JWTs,
queries or prompts as metric labels.

## API unavailable

1. Check `docker compose ... ps` and API container logs.
2. Test `curl -i http://127.0.0.1:8080/readyz`.
3. Inspect PostgreSQL and Redis health because readiness includes them.
4. If only the probe fails, inspect the API listener/network. If both probe and API
   scrape fail, inspect the process and JVM startup.
5. Restore the failed dependency or restart the single failed service after finding
   the cause. Confirm the alert resolves and traffic succeeds.

## Scrape target down

Open Prometheus **Status > Targets**, identify the scrape error, then inspect the
target service and Compose DNS/network. A missing-job alert can indicate an accidental
configuration removal. Validate configuration using `verify-monitoring.sh` before reload.

## API errors or latency

Use the status and latency panels to narrow the time window. Inspect route-template
metrics, JVM GC, Hikari pending requests, database health and queue work. AI endpoints
can naturally be slower than normal API traffic; compare them separately before changing
global thresholds. Correlate with sanitized API logs and reproduce one request.

## Database contention

Check Hikari active/max/pending and PostgreSQL activity. A pending pool can be caused by
slow queries or long transactions, so identify those before increasing the pool. The
learning flow deliberately locks one gamification row per learner; use the lock-duration
panel to distinguish expected short serialization from sustained contention. Never
terminate a production transaction without identifying its owner and impact.

## Embedding queue

Compare ready messages, unacknowledged messages, consumers and processing duration.
If messages exist with zero consumers, inspect API RabbitMQ connectivity and listener
startup. For processing failures, inspect the API exception and the retry count. For
publish failures, content writes may have committed without an embedding event; after
restoring RabbitMQ, update the affected content or run a future reconciliation job.

The declared `gamification_queue` does not currently prove an active worker; do not
enable a consumer alert for it until a consumer is implemented or the queue is removed.

## Resource pressure

For RabbitMQ alarms, check disk and memory first and reduce incoming work if needed.
For Redis evictions, inspect used memory, configured `maxmemory`, cache TTLs and workload.
Do not remove application data or volumes during diagnosis.

## Retention, restart and shutdown

Prometheus keeps at most seven days or 2 GB. Named volumes persist Prometheus, Grafana,
Alertmanager, PostgreSQL and Redis data through ordinary restarts:

```bash
docker compose --env-file backend/.env \
  -f backend/docker-compose.yml \
  -f backend/docker-compose.monitoring.yml down
```

Do not add `--volumes` unless all local state is intentionally disposable. Back up
Grafana changes by exporting them into the version-controlled dashboard JSON; provisioned
dashboards are read-only by design. Review release notes and image compatibility before
upgrading one pinned monitoring image at a time, then rerun full verification.

## Alert delivery and failure drills

The local Alertmanager receiver records alerts without contacting an external service.
Configure Slack/email only through untracked secrets after choosing an authorized
destination. To test delivery safely, use a disposable Compose project and local webhook
receiver. Stop one service, wait for its alert, restore it, and confirm resolution. Never
run outage drills against shared development or production data.

The isolated local stack was verified on 2026-09-25: all seven Prometheus jobs were
healthy, three Grafana dashboards were provisioned, the RabbitMQ embedding queue showed
one consumer, and a controlled API stop caused `EduFlexApiUnavailable` to fire after
its one-minute window and resolve after recovery.
