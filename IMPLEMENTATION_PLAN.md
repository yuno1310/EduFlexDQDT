# Implementation handoff: reliable learning progress and rewards

Prepared 2026-09-23. Implementation started 2026-09-24. See
`docs/reliable-progress-verification.md` for final verification status and evidence.

## Objective and scope

Make lesson completion, daily check-in, and their shared progress/reward writes correct under concurrent requests and retries. Produce integration-test evidence suitable for a Java backend portfolio.

Implement the core milestone below first. Keep Java/XML Android, existing navigation IDs, and API response shapes. Preserve unrelated user changes. Read `AGENTS.md` before editing.

This milestone uses existing PostgreSQL constraints, atomic transitions, and a per-user database row lock. A reward ledger, RabbitMQ outbox, Redis tuning, UI redesign, and framework migration are separate follow-ups. Do not implement them in this task.

## Read these files first; avoid another whole-project scan

Paths below are relative to `backend/src/main/java/com/eduflex/` unless otherwise specified.

| Files | Existing behavior / reason to inspect |
| --- | --- |
| `service/lesson/SaveLessonProgressUseCase.java` | Checks completion before upsert, then awards 20 XP; catches all exceptions inside the transaction. |
| `repository/lesson/LessonProgressRepository.java` | Completion upsert overwrites timestamps; course percentage is calculated from top-level lessons only. |
| `service/gamification/DailyCheckinUseCase.java` | Reads date, awards 10 XP, writes date in separate statements. Called at login and from Android. |
| `repository/gamification/GamificationStatsRepository.java` | XP increment is already an atomic SQL addition; reward eligibility is the unsafe part. |
| `service/gamification/GetGamificationStatsUseCase.java` | First access does check-then-insert, which can race. |
| `service/gamification/{AddXpUseCase,UpdateStreakUseCase,UpdateDailyQuestProgressUseCase}.java` | Shared stats/streak/quest writers must follow the same lock order. |
| `service/quiz/SubmitQuizUseCase.java` | Awards first-pass XP, completes quiz and parent lessons, recalculates progress, and awards course XP. Shares state with normal completion. |
| `repository/enrollment/EnrollmentRepository.java` | Enrollment validation, course completion flag/timestamp, percentage. |
| `service/gamification/{CheckAndAwardBadgesUseCase,AwardBadgeUseCase}.java` and `repository/gamification/{BadgeRepository,UserBadgeRepository}.java` | Badge check-then-insert races, including concurrent users creating the same course badge. |
| `controller/{progress/ProgressController,gamification/GamificationController,quiz/QuizController}.java` | Supplied user IDs need ownership checks. Direct XP endpoint accepts a client-provided amount. |
| `security/JwtAuthFilter.java`, `config/SecurityConfig.java`, `exception/GlobalExceptionHandler.java` | Principal is a UUID; preserve authentication and map authorization errors correctly. |
| `app/src/main/java/com/eduflex/android/api/{ProgressApi,GamificationApi}.java` (repository root) | Lesson completion uses form fields, **not JSON**. Keep this wire format. |

Also read `backend/pom.xml`, `backend/src/main/resources/db/migration/`, `.github/workflows/verify.yml`, and README build instructions as needed.

Known constraints already in migrations:

- `lesson_progress`: unique `(user_id, lesson_id)`.
- `gamification_stats`: unique `user_id`.
- `user_badges`: unique `(user_id, badge_id)`.
- `badges`: unique `name`; `condition_type` is not unique in the initial migration.
- pgvector is required by existing migrations; a plain PostgreSQL image without the extension cannot run the full migration chain.
- Existing backend tests cover AI fallback and JWT behavior, not database concurrency.

## Chosen design

### 1. Transaction and locking contract

- Make stats initialization an `INSERT ... ON CONFLICT (user_id) DO NOTHING`; preserve existing XP and dates.
- Add a small shared repository/service operation that initializes stats and locks that user's stats row with `SELECT ... FOR UPDATE` inside an active Spring transaction.
- All relevant write entry points must acquire this lock **before** reading eligibility or writing progress, attempts, quests, enrollment completion, or rewards. In particular, acquire it at the start of lesson completion and quiz submission, not only inside `AddXpUseCase`.
- Apply the contract to standalone daily check-in, streak updates, quest reward updates, and XP updates too. Nested service calls join the outer transaction; do not use `REQUIRES_NEW`.
- Use one consistent order: user stats lock, then progress/quest/enrollment/reward writes. Review every caller of the modified repository methods for reverse ordering.
- This serializes writes for one learner across application instances, while different learners remain independent. Keep transactions short; do not call Redis, RabbitMQ, AI providers, or other external services while holding the lock.
- Verify Spring proxies and jOOQ use the same transaction/connection. An annotation on a self-invoked method does not establish a new transaction boundary.

### 2. Atomic lesson and course transitions

- Replace the completion upsert with a method returning `true` only when a row is newly completed: insert completed row, or update an existing incomplete row; do nothing for an already completed row.
- Use `ON CONFLICT ... DO UPDATE ... WHERE is_completed IS DISTINCT FROM TRUE RETURNING ...` or equivalent jOOQ. Preserve the original `completed_at` on repeats.
- Award lesson XP only when that method reports a new completion. Existing completed lessons must not receive XP again after deployment.
- Validate lesson existence and enrollment before mutations. Keep the top-level lesson counting rule; reject use of generic completion to bypass a quiz's passing requirement, based on the actual lesson model.
- Recalculate course percentage under the same user lock. Simultaneous completion of different lessons must finish with the correct percentage.
- Make marking an enrollment completed a conditional transition that returns whether it changed. Preserve `completed_at` on repeats.
- Remove the broad catch inside `SaveLessonProgressUseCase`; unexpected failures must escape so Spring rolls back all writes. Map errors at the controller-advice boundary, with no internal exception details exposed to clients.
- Duplicate completion remains a successful no-op with current progress, without a misleading new-XP message.

### 3. Daily reward and time semantics

- Inject a shared `Clock` rather than calling `LocalDate.now()` independently in reward/streak code. Keep the current server zone as the default; permit an explicit configured zone and document it. Tests use a fixed clock.
- Capture the effective date after acquiring the lock. Define one reward per calendar date in that configured zone.
- Prefer one conditional SQL update that increments XP, calculates level from the new XP, and sets `last_login_xp_date` only if its existing value is null or earlier than today. Inspect affected rows to identify whether an award occurred.
- Preserve existing XP formula and the current behavior of daily check-in versus study streak. Handle existing same-day and future-dated records without issuing duplicate rewards.
- Keep the existing date column as the deduplication record; no new ledger or historical XP reconstruction is needed in this milestone.

### 4. Shared quiz and badge paths

- Put `SubmitQuizUseCase` under the same lock before its already-passed check and attempt write. Reuse the atomic completion methods for quiz and parent lessons.
- Preserve existing reward amounts: lesson 20, daily check-in 10, first quiz pass 30, and course completion after a quiz pass 50. Do not start awarding parent-lesson XP merely because a quiz completes its parent.
- Award the 50 course XP on the enrollment's first completed transition when the learner has passed a quiz in that course. This lets a concurrent content completion safely finish a course after the quiz transaction commits, without making rewards depend on thread scheduling. Do not retroactively grant it for an enrollment already completed through another path. Return actual newly awarded XP in responses.
- Repeated quiz attempts remain valid attempts. Exactly-once processing of quiz submissions/quest increments requires a request identity and is **outside this milestone**; do not claim full quiz-request idempotency. The shared locking must still prevent two concurrent requests from awarding the same first-pass, course, or quest-completion reward twice.
- Make user-badge insertion conflict-safe using its existing unique key; normal duplicate awards must not abort the transaction.
- Make dynamic course-badge creation safe across different users. Use a deterministic existing unique key and conflict-safe insert/re-read, or a narrowly justified migration. Preserve existing badge IDs and associations. Do not add a uniqueness constraint over existing data without checking duplicates.

### 5. API ownership and compatibility

- Read learner identity from the authenticated UUID principal. For legacy supplied user IDs, reject mismatches with HTTP 403 and use the principal as the effective identity.
- Apply this to the touched progress, daily check-in, streak, and quiz mutation routes; validate enrollment for lesson/quiz writes. Keep internal login-triggered check-in usable without an HTTP principal.
- Restrict direct client-assigned XP to an explicit admin-only route/permission, preserving internal server-side awards. Inspect callers before changing access; the previous scan found the Android API declaration but no UI caller of `addXp`.
- Preserve the form-encoded `POST /api/progress/lesson` contract. Do not add `@RequestBody` to this endpoint unless deliberately updating and verifying Android too.
- Ensure the generic exception handler does not turn access-denied exceptions into HTTP 500. Add focused authentication/authorization contract tests.

## Implementation order

1. Record `git status`; read the targeted files above. Run baseline backend tests and check Docker availability. Record pre-existing failures separately.
2. Add PostgreSQL integration-test support and fixtures; demonstrate the duplicate-reward/stats-initialization problem with controlled concurrency where practical.
3. Implement conflict-safe stats initialization, the shared lock, atomic completion methods, and conditional daily awards.
4. Wire lesson/daily/streak/quiz/quest entry points, fix badge conflicts and transaction rollback, then enforce ownership without breaking API formats.
5. Complete acceptance tests, required build checks, and documentation. Report exact results and remaining limitations.

Prefer small focused changes. Use existing Spring Boot/jOOQ conventions; do not rewrite generated jOOQ sources by hand or introduce JPA entities for this feature. Regenerate sources only if a schema change actually requires it, using the documented codegen command and a local test database. No migrations are expected for the basic locking design.

## Required acceptance tests

Use JUnit and Testcontainers with a pinned, compatible PostgreSQL + pgvector image. Run real Flyway migrations. Use real repositories and Spring transactions; mock/stub unrelated external providers only. Keep tests discoverable by the existing `./mvnw test` command, for example with `*Test` names.

| Scenario | Required assertion |
| --- | --- |
| 100 concurrent calls completing the same eligible lesson | Exactly one completed row and exactly +20 lesson XP; no duplicate reward messages or unexpected errors. |
| Duplicate completion after response loss | Successful retry, unchanged XP and completion timestamp. |
| Existing completed lesson before upgrade | Retry does not grant XP. |
| Simultaneous completion of two different lessons | Both recorded; correct final percentage; no lost XP. |
| 100 concurrent daily check-ins | Exactly +10 total XP and one date transition. |
| Daily check-in from both login and explicit endpoint | Same daily reward rules; no double award. |
| Fixed-clock same day / next day / zone boundary | Same day no-op; next day grants once; consistent date semantics. |
| Concurrent first access for a new learner | One stats row; no uniqueness exception. |
| Inject failure after progress or XP write | Progress, XP, dates, course flags, and newly created badges roll back together; retry then succeeds once. |
| Concurrent first quiz passes / mixed lesson and quiz completion | First-pass and course rewards at most once; correct progress; response XP matches committed awards. |
| Two learners complete the same course concurrently | One shared course-badge definition and one award per learner; no unique-key failure. |
| Unauthenticated, mismatched user, unenrolled learner, learner calling direct XP | Appropriate 401/403/404 according to documented contract; no mutation. |
| Form-encoded Android progress request | Existing request fields and response fields still work. |

Concurrency harness requirements:

- Invoke real proxied services from independent worker threads/transactions against one database.
- Use a coordinated start and bounded timeouts; collect every failure. Do not use sleeps as synchronization or a latch design that blocks waiting for more workers than the executor can run.
- Commit fixture setup before workers start. Do not wrap the whole test in a test-managed transaction invisible to other connections.
- Size the executor/connection pool deliberately. Record whether 100 means concurrent service requests or simultaneous active database transactions.
- For rollback tests, inject failure after a real write rather than mocking away the database behavior.
- Missing Docker is a verification blocker: report it; do not silently skip integration tests and claim success. CI must actually execute them.

## Verification and deliverables

Run from the appropriate directories:

```bash
cd backend
./mvnw --batch-mode test
```

If app or build configuration changes (including test build/dependency setup), also run from repository root with JDK 17 or 21 and Android SDK 35:

```bash
./gradlew --no-daemon :app:lintDebug :app:assembleDebug :app:assembleRelease :app:bundleRelease
```

- Follow `AGENTS.md`; fix failures without disabling lint, shrinking, or required tests.
- Keep `.github/workflows/verify.yml` and README synchronized when adding Docker/test requirements or changing delivery checks. Keep the normal CI backend test command exercising the integration suite.
- Keep Firebase config, credentials, and signing files untracked. Do not describe unsigned release artifacts as store-ready.
- Add a concise results document under `docs/` with commands, environment, test counts, observed reward totals, and limitations. A correctness test is not a throughput benchmark. Claim p95 latency or throughput only if separately measured with a reproducible workload.
- Report modified files, behavior changes, verification results, and scope limitations. Do not commit or publish unless separately requested.

Suggested CV wording, usable only after verification:

> Implemented concurrency-safe lesson progress and daily rewards with Spring Boot, jOOQ, and PostgreSQL; verified single-reward behavior under 100 concurrent duplicate requests using Testcontainers integration tests.

## Separate follow-ups

1. Reward ledger for auditability: define stable business keys and a migration strategy that avoids re-awarding legacy completions.
2. Idempotency keys for quiz submission so network retries do not create extra attempts or quest increments; validate full quiz-answer ownership/grading as its own review.
3. Transactional outbox, RabbitMQ publisher confirms, idempotent consumers, retry/dead-letter handling, and crash-recovery tests for embedding updates.
4. Targeted cache invalidation and measured hit rates; Android offline progress synchronization and visible sync states.

Do not start these follow-ups until the core milestone is complete and separately requested.
