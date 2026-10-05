# Implementation findings documented in the report

Reviewed on 4 October 2026 against the existing working tree based on `3a71b37`.
These are source-inspection findings. They are not new live exploit tests, and this
report revision does not change application behavior. The earlier 28 passing tests
establish their exercised scenarios; they do not cover every issue below.

## Assessment and reward rules

1. **Incomplete/duplicated multiple-choice submissions.**
   `backend/src/main/java/com/eduflex/service/quiz/SubmitQuizUseCase.java:79`
   uses the submitted list size as the score denominator. The per-item membership
   check is useful but does not require the complete distinct server-owned question
   set. Require complete coverage, unique questions, valid options, and nested item
   validation; add regressions for subsets, duplicates, foreign IDs, and null items.

2. **Fill-blank is a separate feedback-only path.**
   `backend/src/main/java/com/eduflex/service/quiz/SubmitFillBlankUseCase.java:21`
   looks up correct texts by submitted question ID, without enrolment or requested
   lesson scope. It does not persist attempts/progress or award XP. Missing answer
   data can return null and subsequently be dereferenced. Define practice versus
   progression semantics, share authorization/assessment validation, and handle
   missing data deliberately.

3. **Quest activity is client reported.**
   `backend/src/main/java/com/eduflex/controller/gamification/DailyQuestController.java:34`
   accepts the owner's quest type and integer increment. One reward per date does
   not establish authentic activity. Derive quiz events on the server and define
   bounded, replay-safe study-time policy.

4. **Course reward eligibility can be lost before quiz passage.**
   `backend/src/main/java/com/eduflex/service/lesson/SaveLessonProgressUseCase.java:86`
   can complete an enrolment before any quiz pass; the content-path course bonus
   requires a previous pass. A later quiz sees an already-completed course. The
   report works through A/B/Q orderings producing 100, 120, or 70 learning XP,
   excluding independent daily/quest rewards. These are source-derived examples,
   not freshly executed tests. Select the intended policy and test all orderings.

5. **Progress and assessment mastery are different.**
   `backend/src/main/java/com/eduflex/repository/lesson/LessonProgressRepository.java`
   counts only top-level lessons. A child quiz's first pass also completes its
   parent. The current percentage does not prove every child assessment was passed.

## API policy and client boundaries

6. **Review policies differ.**
   `SubmitReviewUseCase` requires enrolment; `SubmitCourseReviewUseCase` does not.
   Their controller routes share storage but differ in eligibility. Select one
   canonical rule and verify compatibility routes against it.

7. **Content reads lack course entitlement checks.**
   `LessonController`/`GetLessonUseCase` and `QuizController`/`GetQuizUseCase`
   require authentication through the security chain but do not check enrolment in
   those read paths. AI and progress paths have additional checks. If paid content
   protection is intended, exercise an enrolled/non-enrolled/administrator matrix.

8. **Development transport and session defaults need a production policy.**
   The main Android manifest permits cleartext traffic and application backup.
   `app/src/main/java/com/eduflex/android/auth/TokenManager.java:24` uses ordinary
   private SharedPreferences. Disabled release HTTP logging does not establish
   encrypted transport or a complete credential storage/backup policy.

9. **Study-time delivery is best effort.**
   `app/src/main/java/com/eduflex/android/ui/lesson/LessonStudyFragment.java:60`
   submits foreground elapsed time on pause; failure is logged without durable
   replay. Foreground time does not prove attention. Persistent delivery requires
   event identity and server deduplication to avoid double credit.

## Persistence, caching, and infrastructure

10. **Missing cache dependencies.**
    Course reads cache an outer `courses` response and inner `courseCatalog` query.
    Course writes clear the latter but omit the former. Lesson creation omits the
    `lessons` cache, although update/delete clear it. Enrolment does not clear
    learner-specific search results that exclude enrolled courses. Regressions
    should warm the route, mutate, immediately reread, and verify current content.
    Configured TTL names are also distinguished from actively cached endpoints.

11. **Rollback scope is not universal.**
    `backend/src/main/java/com/eduflex/service/lesson/CreateLessonUseCase.java:63`
    catches broad exceptions within a transactional service. Returning a failure
    DTO is not independently an instruction to roll back successful earlier work.
    Inject failure between parent/child writes and verify the desired atomic result.

12. **Clean migration success does not prove populated upgrades.**
    Migration 1.2.7 deletes duplicate successful transaction records before adding
    the partial unique index. Rehearse upgrades from representative older data with
    a backup and explicit affected-row/relationship checks. Reviews and transactions
    also have non-cascading dependencies, unlike several learning tables.

13. **Optional generation default has expired upstream.**
    `application.properties` and `AiCourseService` default to `gemini-2.0-flash`.
    [Google's release notes](https://ai.google.dev/gemini-api/docs/changelog) record
    shutdown on 1 June 2026. Select a supported model using `GEMINI_MODEL`, check
    request/response compatibility with credentials, and verify bounded failures.
    No credentialed provider test was performed in this report task.

14. **Derived-state recovery remains incomplete.**
    After-commit publication has no outbox or broker confirmation; base Compose has
    no RabbitMQ data volume. There is no version guard for vector writes or explicit
    replay/dead-letter tool. A configured gamification queue has no reward consumer.
    Recovery/backup diagrams are explicitly proposed rather than executed drills.

## Priority for a later application task

Start with assessment correctness, trusted reward activity, the approved course
completion/reward policy, entitlement alignment, and missing cache invalidation.
Then verify optional provider configuration and transactional content failure paths.
Outbox/reconciliation, backup restoration, device coverage, and controlled performance
measurement remain separate engineering increments with acceptance evidence in the
report roadmap.
