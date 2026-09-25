#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
PROJECT_NAME="${MONITORING_PROJECT_NAME:-eduflex-monitoring-check}"
COMPOSE=(docker compose --project-name "${PROJECT_NAME}" --env-file "${BACKEND_DIR}/.env" -f "${BACKEND_DIR}/docker-compose.yml" -f "${BACKEND_DIR}/docker-compose.monitoring.yml")

for command_name in docker curl jq; do
  command -v "${command_name}" >/dev/null || { echo "Missing required command: ${command_name}" >&2; exit 1; }
done

cleanup() {
  status=$?
  if [[ "${CLEANUP_MONITORING:-false}" == "true" && "${status}" == "0" ]]; then
    "${COMPOSE[@]}" down --volumes --remove-orphans
  fi
  return "${status}"
}
trap cleanup EXIT

"${COMPOSE[@]}" config --quiet
"${COMPOSE[@]}" run --rm --no-deps --entrypoint promtool eduflex-prometheus check config /etc/prometheus/prometheus.yml
"${COMPOSE[@]}" run --rm --no-deps --entrypoint promtool eduflex-prometheus check rules /etc/prometheus/rules/eduflex.yml
"${COMPOSE[@]}" run --rm --no-deps --entrypoint promtool eduflex-prometheus test rules /etc/prometheus/tests/eduflex.test.yml
"${COMPOSE[@]}" up --build --detach

deadline=$((SECONDS + 180))
until curl --fail --silent http://127.0.0.1:9090/-/ready >/dev/null \
  && curl --fail --silent http://127.0.0.1:3000/api/health >/dev/null; do
  if (( SECONDS >= deadline )); then
    "${COMPOSE[@]}" ps
    echo "Monitoring stack did not become ready within 180 seconds" >&2
    exit 1
  fi
  sleep 3
done

curl --fail --silent --request POST http://127.0.0.1:9090/-/reload >/dev/null

for job in eduflex-api eduflex-api-probe postgres redis rabbitmq rabbitmq-queues prometheus; do
  query="count(up{job=\"${job}\"} == 1)"
  deadline=$((SECONDS + 120))
  while true; do
    value="$(curl --fail --silent --get --data-urlencode "query=${query}" http://127.0.0.1:9090/api/v1/query | jq -r '.data.result[0].value[1] // "0"')"
    [[ "${value}" != "0" ]] && break
    if (( SECONDS >= deadline )); then
      echo "Prometheus job is not healthy: ${job}" >&2
      exit 1
    fi
    sleep 3
  done
done

public_metrics_status="$(curl --silent --output /dev/null --write-out '%{http_code}' http://127.0.0.1:8080/actuator/prometheus)"
[[ "${public_metrics_status}" != "200" ]] || { echo "Metrics are exposed on the public API port" >&2; exit 1; }

grafana_user="${GRAFANA_ADMIN_USER:-admin}"
grafana_password="${GRAFANA_ADMIN_PASSWORD:-eduflex-local}"
for uid in eduflex-overview eduflex-dependencies eduflex-learning; do
  curl --fail --silent --user "${grafana_user}:${grafana_password}" \
    "http://127.0.0.1:3000/api/dashboards/uid/${uid}" >/dev/null
done

curl --fail --silent http://127.0.0.1:8080/readyz | jq -e '.status == "UP"' >/dev/null
echo "EduFlex monitoring verification passed"
