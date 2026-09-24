# Reliable progress and rewards: verification

Verified on 2026-09-24 with Java 21, Docker Engine 29.8.1, PostgreSQL 16 +
pgvector, Android SDK 35, and Build Tools 34.0.0.

## Result

Learning progress and reward writes are serialized per learner with a PostgreSQL
row lock. The database now decides whether lesson, daily reward, enrollment, and
badge transitions are new, so a retry does not repeat their reward. The configured
calendar defaults to `Asia/Ho_Chi_Minh` and can be changed with
`EDUFLEX_TIME_ZONE`.

The integration suite starts a clean pgvector PostgreSQL database with
Testcontainers, applies all 27 Flyway migrations, and runs real Spring transactions
and jOOQ repositories. The image is pinned by digest.

Verified behavior includes:

- 100 concurrent service invocations for the same lesson: one progress row and
  exactly 20 XP. The harness starts 100 worker threads together; the test Hikari
  pool permits up to 32 simultaneous database transactions.
- 100 concurrent daily check-ins: exactly 10 XP and one calendar-date transition.
- 100 concurrent first stats reads: one stats row without uniqueness failures.
- Retry after a successful lesson completion preserves `completed_at` and XP.
- A lesson already completed before this change does not receive retroactive XP.
- Concurrent completion of different lessons preserves both rewards and ends at
  the correct course percentage.
- Concurrent first quiz passes award first-pass and course XP once; mixed content
  and quiz completion keeps the final progress and XP totals correct.
- Quiz-first ordering followed by the final content lesson grants the course reward
  exactly once, including a repeated completion request.
- Two learners completing the same course concurrently create one course-badge
  definition and one award for each learner.
- An injected failure after progress, XP, enrollment, and badge writes rolls the
  transaction back; a retry then succeeds once.
- Login and explicit check-in share one daily reward. Same-day, next-day, local
  time-zone boundary, and future-dated records are covered with a fixed clock.
- Existing form-encoded Android progress requests remain compatible. Unauthenticated,
  wrong-owner, unenrolled, and learner-assigned-XP requests are rejected without a
  progress/reward mutation.

## Commands and observed results

```bash
cd backend
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw --batch-mode test
```

Result: **BUILD SUCCESS**, 19 tests, 0 failures, 0 errors, 0 skipped. Of these,
17 are PostgreSQL integration scenarios in `ProgressConcurrencyIntegrationTest`.

```bash
ANDROID_HOME=/tmp/eduflex-android-sdk \
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 \
./gradlew --no-daemon :app:lintDebug :app:assembleDebug \
  :app:assembleRelease :app:bundleRelease
```

Result: **BUILD SUCCESS**, including debug lint, debug APK, release APK, and release
AAB with release shrinking enabled. The release APK and AAB are unsigned inspection
artifacts and are not store-ready. Android Gradle Plugin 8.5.2 emitted its existing
compileSdk 35 support warning; it did not fail lint or either build variant.

## Limits

The per-user lock deliberately serializes writes for one learner; different learners
can still update independently. The suite proves correctness and rollback behavior,
not throughput or p95 latency.

Quiz submissions do not yet accept an idempotency key. A response-loss retry can
therefore create another quiz-attempt row and increment an unfinished daily quest,
although first-pass XP, course XP, quest-completion XP, progress, and badges remain
protected from duplicate awards. A reward ledger and RabbitMQ transactional outbox
remain separate follow-up work.

## CV wording

> Implemented concurrency-safe lesson progress and daily rewards with Spring Boot,
> jOOQ, and PostgreSQL; verified single-reward behavior under 100 concurrent duplicate
> requests using Testcontainers integration tests.
