#!/usr/bin/env bash
set -euo pipefail

API_URL="${API_URL:-http://localhost:8080}"
COMPOSE_FILE="${COMPOSE_FILE:-backend/docker-compose.yml}"
ENV_FILE="${ENV_FILE:-backend/.env}"
RUN_ID="$(date +%s)-$$"
ADMIN_EMAIL="journey-admin-${RUN_ID}@example.test"
LEARNER_EMAIL="journey-learner-${RUN_ID}@example.test"
PASSWORD='Journey1!Pass'
COURSE_TITLE="Journey course ${RUN_ID}"
CHECKS=0

need() { command -v "$1" >/dev/null || { echo "Missing required command: $1" >&2; exit 2; }; }
need curl
need jq
need docker

api() {
  local method="$1" path="$2" expected="$3" body="${4:-}" token="${5:-}"
  local response_file status
  response_file="$(mktemp)"
  local args=(-sS -o "$response_file" -w '%{http_code}' -X "$method" "$API_URL$path")
  if [[ -n "$body" ]]; then args+=(-H 'Content-Type: application/json' --data "$body"); fi
  if [[ -n "$token" ]]; then args+=(-H "Authorization: Bearer $token"); fi
  status="$(curl "${args[@]}")"
  if [[ "$status" != "$expected" ]]; then
    echo "FAILED $method $path: expected $expected, got $status" >&2
    sed -n '1,20p' "$response_file" >&2
    rm -f "$response_file"
    exit 1
  fi
  CHECKS=$((CHECKS + 1))
  cat "$response_file"
  rm -f "$response_file"
}

db_value() {
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T eduflex-postgres \
    sh -c 'psql -qtAX -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 -c "$1"' sh "$1"
}

cleanup() {
  db_value "DELETE FROM courses WHERE title = '$COURSE_TITLE'; DELETE FROM users WHERE email IN ('$ADMIN_EMAIL', '$LEARNER_EMAIL');" >/dev/null 2>&1 || true
}
trap cleanup EXIT

api GET /readyz 200 >/dev/null
api POST /api/user/register 200 "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$PASSWORD\",\"name\":\"Journey Admin\",\"active\":true}" >/dev/null
api POST /api/user/register 200 "{\"email\":\"$LEARNER_EMAIL\",\"password\":\"$PASSWORD\",\"name\":\"Journey Learner\",\"active\":true}" >/dev/null

db_value "UPDATE users SET role = 'admin' WHERE email = '$ADMIN_EMAIL';" >/dev/null
ADMIN_LOGIN="$(api POST /api/user/login 200 "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$PASSWORD\"}")"
ADMIN_ACCESS="$(jq -er '.accessToken' <<<"$ADMIN_LOGIN")"
LEARNER_LOGIN="$(api POST /api/user/login 200 "{\"email\":\"$LEARNER_EMAIL\",\"password\":\"$PASSWORD\"}")"
LEARNER_ACCESS="$(jq -er '.accessToken' <<<"$LEARNER_LOGIN")"
LEARNER_REFRESH="$(jq -er '.refreshToken' <<<"$LEARNER_LOGIN")"
LEARNER_ID="$(db_value "SELECT user_id FROM users WHERE email = '$LEARNER_EMAIL';")"

api GET /api/course 401 >/dev/null
api PUT "/api/user/update-profile/$LEARNER_ID" 403 '{"fullName":"Blocked"}' "$ADMIN_ACCESS" >/dev/null
api POST /api/user/forgot-password 503 "{\"email\":\"$LEARNER_EMAIL\"}" >/dev/null
api POST /api/course 403 "{\"title\":\"Blocked\",\"learningModel\":\"self-paced\",\"status\":\"active\",\"price\":100}" "$LEARNER_ACCESS" >/dev/null
api POST /api/course 200 "{\"title\":\"$COURSE_TITLE\",\"learningModel\":\"self-paced\",\"status\":\"active\",\"description\":\"Reproducible journey\",\"price\":2500}" "$ADMIN_ACCESS" >/dev/null
COURSE_ID="$(db_value "SELECT course_id FROM courses WHERE title = '$COURSE_TITLE';")"

api POST "/api/enrollment/$COURSE_ID/register" 400 "{\"userId\":\"$LEARNER_ID\"}" "$LEARNER_ACCESS" >/dev/null
PAYMENT_BODY="{\"userId\":\"$LEARNER_ID\",\"courseId\":\"$COURSE_ID\"}"
api POST /api/payment 403 "$PAYMENT_BODY" "$ADMIN_ACCESS" >/dev/null
api POST /api/payment 200 "$PAYMENT_BODY" "$LEARNER_ACCESS" | jq -e '.simulated == true' >/dev/null
api POST /api/payment 200 "$PAYMENT_BODY" "$LEARNER_ACCESS" >/dev/null
[[ "$(db_value "SELECT count(*) FROM transactions WHERE user_id = '$LEARNER_ID' AND course_id = '$COURSE_ID' AND status = 'SUCCESS';")" == 1 ]]
CHECKS=$((CHECKS + 1))
[[ "$(db_value "SELECT count(*) FROM enrollments WHERE user_id = '$LEARNER_ID' AND course_id = '$COURSE_ID';")" == 1 ]]
CHECKS=$((CHECKS + 1))

api GET "/api/course/$COURSE_ID/ai-summary" 200 '' "$LEARNER_ACCESS" >/dev/null
api GET "/api/course/$COURSE_ID/ai-summary" 200 '' "$ADMIN_ACCESS" >/dev/null

LESSON_JSON="$(curl -sS -X POST "$API_URL/api/lesson" -H "Authorization: Bearer $ADMIN_ACCESS" \
  --data-urlencode "courseID=$COURSE_ID" --data-urlencode 'title=Journey lesson' \
  --data-urlencode 'contentType=text' --data-urlencode 'content=Verified lesson content')"
LESSON_ID="$(jq -er '.lessonId' <<<"$LESSON_JSON")"
CHECKS=$((CHECKS + 1))
for ignored in 1 2; do
  status="$(curl -sS -o /dev/null -w '%{http_code}' -X POST "$API_URL/api/progress/lesson" \
    -H "Authorization: Bearer $LEARNER_ACCESS" --data-urlencode "lessonId=$LESSON_ID" \
    --data-urlencode "userId=$LEARNER_ID")"
  [[ "$status" == 200 ]]
  CHECKS=$((CHECKS + 1))
done
[[ "$(db_value "SELECT count(*) FROM lesson_progress WHERE user_id = '$LEARNER_ID' AND lesson_id = '$LESSON_ID';")" == 1 ]]
CHECKS=$((CHECKS + 1))

REFRESHED="$(api POST /api/auth/refresh 200 "{\"refreshToken\":\"$LEARNER_REFRESH\"}")"
jq -e '.accessToken | length > 20' <<<"$REFRESHED" >/dev/null
api POST /api/auth/logout 200 "{\"refreshToken\":\"$LEARNER_REFRESH\"}" >/dev/null
api POST /api/auth/refresh 401 "{\"refreshToken\":\"$LEARNER_REFRESH\"}" >/dev/null

echo "HTTP journey passed: $CHECKS checks"
