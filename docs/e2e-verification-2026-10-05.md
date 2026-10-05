# EduFlex E2E verification — October 5, 2026

The main Android learning journey reaches the live Spring Boot API and PostgreSQL, but the project does **not** pass all E2E requirements. The broader API/database runner passed **59 of 70 assertions**. Mobile testing reproduced stale progress and administration lists, and a certificate that stays locked after local data is cleared despite 100% completion in PostgreSQL.

Two password-recovery blockers discovered during this run were fixed and retested: the Android email input was hidden, and reset confirmation returned HTTP 500 because a nested jOOQ transaction requested unsupported JPA savepoints. Other findings below remain open.

This is a new verification snapshot of commit `038a21e` plus the local recovery fixes and verification scripts. It does not replace the older dated verification evidence or the report PDF's historical measurements.

## Environment and evidence

- Native Android debug app installed on the `eduflex-e2e-api35` emulator: API 35, x86_64, 1080 × 2400, standard font settings. App API URL: `http://10.0.2.2:8080/`.
- JDK 21; Android SDK 35 at `/home/yuno/.cache/eduflex-android-sdk`.
- Rebuilt API in Docker Desktop's `desktop-linux` context, with PostgreSQL 16/pgvector, Redis, RabbitMQ and the monitoring Compose overlay.
- All 28 Flyway migrations applied. No production deployment or real payment was performed.
- Password-recovery delivery temporarily enabled against a dedicated local Mailpit container. SMTP auth and TLS disabled for this isolated container network; Gemini API key empty during those checks. Original API environment restored afterwards.
- Existing monitoring containers from a previous run on Docker's `default` context occupied ports 3000/9090/9093. Only those three EduFlex monitoring containers were stopped; the Docker Desktop monitoring stack then started successfully. Unrelated containers and existing database volumes were preserved.

Checked-in [API assertions](e2e/2026-10-05/stack-regressions.json), [mobile assertions](e2e/2026-10-05/ui-results.json), [recovery assertions](e2e/2026-10-05/smtp-results.json), [monitoring assertions](e2e/2026-10-05/monitoring-results.json), [additional database evidence](e2e/2026-10-05/failure-evidence.json), [build results](e2e/2026-10-05/build-results.json), and [fixture cleanup results](e2e/2026-10-05/cleanup-results.json) record expected and observed outcomes. [Screenshots](e2e/2026-10-05/screenshots/) show the relevant app states. Fixture session tokens, reset-code contents and mailbox messages are excluded.

## Verification results

| Layer | Result | Scope |
| --- | --- | --- |
| Backend automated suite | 32 tests, 0 failures/errors/skips | Real PostgreSQL through Testcontainers; four new reset endpoint regressions |
| Android unit suite | 2 tests, no failures/errors | Login response mapping and synchronized refresh coordination |
| Android builds and lint | Passed | Debug lint/APK, release APK/AAB with shrinking enabled; lint has 0 errors and 369 warnings |
| Existing HTTP journey | 25 checks passed | Registration, authorization, simulated purchase, lesson progress, refresh/logout, database counts and fixture cleanup |
| Broader live API/database regression | 59/70 passed | Happy paths, negative security/business assertions, cache consistency, admin CRUD and 100 concurrent requests |
| Android acceptance | 19/24 recorded assertions passed | Actual screen actions with database verification; see coverage and failures below |
| SMTP-backed recovery | 12/12 assertions passed after fixes | Local delivery, hashing, invalid/weak/expired/reused codes, password changes and session revocation |
| Monitoring | 10/10 recorded assertions passed | Seven healthy scrape targets and three provisioned dashboards; configuration and rule tests also passed |

The first Android verification forced all 115 Gradle tasks to rerun and succeeded. After the layout fix, the complete required build/lint command ran again and succeeded. Release artifacts remain unsigned; E2E UI acceptance used the debug APK.

The broader runner's failing checks intentionally express enrollment, complete-quiz validation, distinct-activity reward and immediate-cache-consistency requirements. Some requirements are not implemented by the current API; its nonzero exit status exposes these gaps rather than asserting that the existing implementation must already satisfy them. Counts across suites overlap and should not be added into one coverage percentage.

## Android → API → database coverage

| Workflow | What was actually exercised | Observed outcome |
| --- | --- | --- |
| Registration and login | Entered a new fixture name/email/password in the Android registration screen, then logged in | Account persisted; Home loaded live XP/streak and quests |
| Learning empty state | Opened Learn before enrollment | Empty message and browse action displayed |
| Discovery | Searched for the fixture paid course and opened its details | API results rendered and course opened |
| Cart and checkout | Added course, opened cart, selected bank-transfer simulation, submitted checkout | One `SUCCESS` transaction and one enrollment; cart emptied |
| Enrolled course and lesson | Opened course from Learn and read fixture transaction content | Backend lesson text rendered |
| Course summary | Opened the course learning brief | Grounded fallback brief rendered; no claim of live Gemini generation |
| Multiple-choice quiz | Selected both correct answers and submitted in Android | One attempt; 100% course progress; +80 quiz/course XP; total XP 90 including login; one course badge |
| Review | Selected rating, entered comment and submitted | Comment persisted in `course_reviews` |
| Profile | Edited and saved learner name | `users.full_name` changed to the submitted name |
| Logout and recovery | Logged out; requested local SMTP code; entered code/new password; logged in again | Password changed and code consumed once; old password/code/earlier refresh session rejected |
| Session recovery | Replaced only the emulator's access-token signature with an invalid signature, preserving its refresh session, then restarted the app | Rejected access token replaced; session remained signed in. This tests rejected-token recovery, not a timed natural-expiry soak |
| API outage and Retry | Stopped only the API container, opened Learn, restarted the API, tapped Retry | Visible Retry state; enrollment reloaded; session survived |
| Certificate | Opened after a fresh sign-in with local progress still available, then repeated after clearing emulator app data | First case worked; after data clear the course showed 100% but certificate was locked |
| Admin screens and course creation | Logged in as fixture admin, opened users/courses, created a course through its dialog | Admin screens loaded and course persisted; new course absent from refreshed list |
| Admin edit | Evicted only that fixture admin's outer catalogue cache and restarted app, then edited the created course | Edit persisted. The explicit cache workaround is **not** counted as a natural list-refresh success |
| Admin lesson creation | Opened lesson management and created a text lesson through its dialog | Lesson persisted; new lesson absent from the cached list |

A generated empty quiz displayed the generic “Failed to load quiz” message and a disabled Submit action. Free-course Android checkout also persisted enrollment. It is an incomplete course-authoring fixture; reading-only completion is not established by this UI check. `ProgressApi.saveLessonProgress` exists in the app but currently has no screen caller; content-only completion is verified through HTTP and PostgreSQL, not through an Android completion action.

The UI checks used ADB input and UIAutomator hierarchy dumps. Android has no checked-in instrumentation suite, so these observations are acceptance evidence, not a fully unattended Android CI suite. Harness problems involving notification permission dialogs, off-screen controls, matching a search input instead of a result, and mailbox CRLF normalization were corrected before recording the final application outcomes.

## Open findings and reproduction

| ID | Priority | Reproduction and observed result | Required improvement |
| --- | --- | --- | --- |
| E2E-01 | High | As a signed-in, unenrolled user, GET `/api/lesson?courseID=<paidCourse>` and GET `/api/quiz/<quizLesson>` both return 200 | Enforce paid-course entitlement before disclosing lesson/quiz content; keep admin access |
| E2E-02 | High | POST `/api/quiz/submit-multiple-choice` with one correct answer for a two-question quiz returns 200 and `passed=true` | Validate unique answers against the authoritative question set and calculate score from that set |
| E2E-03 | High | An unenrolled caller submits to `/api/quiz/fill-blank` using a question ID from another course; response is 200 | Check enrollment, lesson/question membership and supported quiz type before evaluating or returning answer details |
| E2E-04 | High | Owner calls daily quest progress with `questType=QUIZ_COUNT, increment=10` and no quiz attempts | Reject client-authored quiz activity; derive quiz progress from verified server events. Database evidence: 0 attempts, completed 10/10 quest, +100 quest XP, total XP 110 including login |
| E2E-05 | Medium | Three successful submissions of the same quiz advance `PERFECT_RUN`; third yields another 60 XP although `xpRewarded` for completion is 0 | Decide and enforce whether daily quests count distinct quizzes or repeat attempts. Current completion XP is idempotent; daily quest activity is not distinct-quiz constrained |
| E2E-06 | Medium | Nested answer with null `questionId`/`selectedOptionId` returns 200 | Cascade validation into answer elements and reject malformed submissions before writing attempts/quests |
| E2E-07 | Medium | Unenrolled user POSTs a valid review to `/api/course/<courseId>/reviews`; response 200 | Apply the same enrollment rule as the other review endpoint |
| E2E-08 | High | Warm GET `/api/course`; admin edits a course; PostgreSQL has the edit but the same caller gets the old catalogue. Admin UI creation also persists without appearing on refresh | Invalidate the outer `courses` cache as well as `courseCatalog`, with tests of the actual cached read path |
| E2E-09 | Medium | Warm a lesson list; admin creates a lesson; database gains the lesson but GET/list refresh remains unchanged | Invalidate `lessons` on creation, including the generated quiz child |
| E2E-10 | High | Complete a quiz in Android: PostgreSQL enrollment is 100%; returning to the existing course-detail navigation entry still shows 0% and blocks certificate navigation | Refresh authoritative progress when returning; stop relying on the original navigation argument |
| E2E-11 | High | Clear emulator app data and sign in again: course detail loads 100% from backend, but certificate displays “Certificate is locked” | Determine certificate eligibility from server completion rather than a missing local `course_progress` preference |

The 11 failing API assertions correspond to E2E-01 through E2E-09; some issues have multiple assertions. Mobile failures additionally show E2E-08 through E2E-11. These are reproduced local defects or explicitly unmet business-hardening requirements, not production incident claims.

Useful mobile evidence: [quiz success](e2e/2026-10-05/screenshots/12-quiz-result.png), [stale progress afterwards](e2e/2026-10-05/screenshots/14-stale-course-progress.png), [fresh-install certificate inconsistency](e2e/2026-10-05/screenshots/30-fresh-install-certificate.png), [API failure state](e2e/2026-10-05/screenshots/23-api-outage.png), and [successful retry](e2e/2026-10-05/screenshots/24-outage-recovered.png).

## Fixes made during verification

- Moved `til_new_password` and its initial hidden state from the email container to the password container in `activity_forgot_password.xml`. The email field is now available before requesting a code; code/password fields appear after successful delivery.
- Changed `PasswordResetTokenRepository.consume` to join the Spring service transaction with `@Transactional`, preserving row locking and atomic password/token writes without requesting an unsupported nested jOOQ transaction.
- Added PostgreSQL-backed reset endpoint tests for unknown codes, password update and reuse, expired codes without mutation, and 20 simultaneous confirmations with exactly one success.
- Fixed HTTP-journey check counting across shell subshells and removal of payment/review/badge dependencies before fixture deletion; cleanup failures now fail the run.
- Added `verify-stack-regressions.py`, a standard-library Python runner that stores outcomes without session tokens and returns nonzero when requirements fail.

## Repeatable commands

From the project root, with the backend `.env` configured and Docker running:

```bash
docker compose --env-file backend/.env \
  -f backend/docker-compose.yml -f backend/docker-compose.monitoring.yml up -d --build

JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 \
  bash -c 'cd backend && ./mvnw --batch-mode test'

JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 \
ANDROID_HOME=/home/yuno/.cache/eduflex-android-sdk \
  ./gradlew --no-daemon :app:lintDebug :app:testDebugUnitTest \
  :app:assembleDebug :app:assembleRelease :app:bundleRelease

bash backend/scripts/verify-http-journey.sh
python3 backend/scripts/verify-stack-regressions.py \
  --report /tmp/eduflex-stack-regressions.json
```

The HTTP journey expects default disabled reset delivery (HTTP 503). SMTP acceptance requires a local mailbox plus a temporary API environment override; it should not use someone else's real mailbox. The broader runner needs no mailbox and documents `--api-url`, `--env-file`, `--compose-file`, and `--project-name` in `--help`. With the current findings it exits 1 and reports 59/70, while still performing fixture cleanup.

The runner checks the real PostgreSQL container through `psql`; a 200 response alone does not establish persistence. Its 100 live completion requests must all return 200, create one progress row, and add exactly 20 XP. Cleanup removes only the generated accounts/courses and their dependent records and session/cache keys; shared catalogue/search caches are invalidated to remove fixture results. Existing volumes and non-fixture data are retained.

## Limits and remaining work

This run does not verify every Android version, physical device, rotation/background scenario, enlarged font size, or every permutation of navigation and concurrent users. Admin course/lesson authoring was driven through Android; question CRUD was verified through the API and database, and quiz consumption through Android. Admin deletion combinations with existing real enrollments/reviews/payments were not exhaustively driven through the UI.

Supabase uploads, Firebase push, external YouTube playback, live Gemini provider output, real payment gateways, production SMTP/TLS, and signed/store distribution remain outside this local verification. The payment flow is explicitly a development simulation. Prometheus configuration, 13 rules and rule unit tests passed; seven targets and three dashboards were checked after recovery, but a full alert-notification delivery/soak drill was not repeated.

The next implementation work should first close entitlement/quiz/quest security gaps, then make catalogue/lesson cache eviction and course/certificate progress consistent across devices. Repeat the regression runner and mobile scenarios after those fixes; passing today's unit tests does not close the findings above.
