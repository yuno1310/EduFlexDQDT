package com.eduflex.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.eduflex.dto.lesson.ProgressDTO.SaveLessonRequest;
import com.eduflex.dto.quiz.QuizDTO.AnswerItem;
import com.eduflex.dto.quiz.QuizDTO.SubmitQuizRequest;
import com.eduflex.dto.quiz.QuizDTO.SubmitQuizResponse;
import com.eduflex.dto.user.LogInDTO.LogInRequest;
import com.eduflex.repository.enrollment.EnrollmentRepository;
import com.eduflex.repository.gamification.GamificationStatsRepository;
import com.eduflex.repository.lesson.LessonProgressRepository;
import com.eduflex.security.RefreshTokenService;
import com.eduflex.service.gamification.CheckAndAwardBadgesUseCase;
import com.eduflex.service.gamification.DailyCheckinUseCase;
import com.eduflex.service.gamification.GetGamificationStatsUseCase;
import com.eduflex.service.lesson.SaveLessonProgressUseCase;
import com.eduflex.service.quiz.SubmitQuizUseCase;
import com.eduflex.service.user.LogInUseCase;
import org.springframework.transaction.annotation.Transactional;

@Testcontainers
@SpringBootTest(properties = {
    "spring.rabbitmq.listener.simple.auto-startup=false",
    "spring.cache.type=none",
    "spring.datasource.hikari.maximum-pool-size=32",
    "JwtSecret=integration-test-secret-that-is-at-least-32-bytes",
    "JwtExpirationMs=900000"
})
@AutoConfigureMockMvc
class ProgressConcurrencyIntegrationTest {

  private static final ZoneId TEST_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
      DockerImageName.parse("pgvector/pgvector:pg16@sha256:ccc6e83d6e35e931dc7c5def2022729d5a6c370318d099181995567ff1fb4d6b")
          .asCompatibleSubstituteFor("postgres"));

  @DynamicPropertySource
  static void databaseProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @TestConfiguration
  static class TestClockConfiguration {
    @Bean
    @Primary
    MutableClock mutableClock() {
      return new MutableClock(Instant.parse("2026-09-24T02:00:00Z"), TEST_ZONE);
    }

    @Bean
    RollbackProbe rollbackProbe(GamificationStatsRepository stats,
        LessonProgressRepository progress, EnrollmentRepository enrollments,
        CheckAndAwardBadgesUseCase badges) {
      return new RollbackProbe(stats, progress, enrollments, badges);
    }
  }

  static class RollbackProbe {
    private final GamificationStatsRepository stats;
    private final LessonProgressRepository progress;
    private final EnrollmentRepository enrollments;
    private final CheckAndAwardBadgesUseCase badges;

    RollbackProbe(GamificationStatsRepository stats, LessonProgressRepository progress,
        EnrollmentRepository enrollments, CheckAndAwardBadgesUseCase badges) {
      this.stats = stats;
      this.progress = progress;
      this.enrollments = enrollments;
      this.badges = badges;
    }

    @Transactional
    public void writeThenFail(UUID userId, UUID courseId, UUID lessonId) {
      stats.ensureAndLock(userId);
      progress.completeLessonIfNeeded(userId, lessonId);
      stats.updateXpAndLevel(userId, 20);
      enrollments.markCourseAsCompletedIfNeeded(userId, courseId);
      badges.checkCourseCompletionBadge(userId, courseId);
      throw new IllegalStateException("injected failure after database writes");
    }
  }

  static final class MutableClock extends Clock {
    private final AtomicReference<Instant> instant;
    private final ZoneId zone;

    MutableClock(Instant instant, ZoneId zone) {
      this.instant = new AtomicReference<>(instant);
      this.zone = zone;
    }

    void setInstant(Instant newInstant) {
      instant.set(newInstant);
    }

    @Override
    public ZoneId getZone() {
      return zone;
    }

    @Override
    public Clock withZone(ZoneId newZone) {
      return new MutableClock(instant(), newZone);
    }

    @Override
    public Instant instant() {
      return instant.get();
    }
  }

  @Autowired DSLContext dsl;
  @Autowired SaveLessonProgressUseCase saveLessonProgress;
  @Autowired DailyCheckinUseCase dailyCheckin;
  @Autowired GetGamificationStatsUseCase getStats;
  @Autowired GamificationStatsRepository statsRepository;
  @Autowired SubmitQuizUseCase submitQuiz;
  @Autowired RollbackProbe rollbackProbe;
  @Autowired MutableClock clock;
  @Autowired MockMvc mockMvc;
  @Autowired LogInUseCase logIn;
  @Autowired PasswordEncoder passwordEncoder;

  @MockitoBean RefreshTokenService refreshTokenService;

  @BeforeEach
  void resetDatabaseAndClock() {
    dsl.execute("TRUNCATE TABLE user_badges, quiz_attempts, user_daily_quest_progress, "
        + "lesson_progress, enrollments, questions, lesson, gamification_stats, courses, users "
        + "RESTART IDENTITY CASCADE");
    dsl.execute("DELETE FROM badges WHERE condition_type LIKE 'COURSE_%'");
    clock.setInstant(Instant.parse("2026-09-24T02:00:00Z"));
  }

  @Test
  void oneHundredConcurrentDuplicateCompletionsAwardLessonXpOnce() throws Exception {
    Fixture fixture = createFixture(1);

    List<Boolean> successes = concurrently(100,
        () -> saveLessonProgress.execute(new SaveLessonRequest(fixture.lessonIds().get(0), fixture.userId()))
            .success());

    assertThat(successes).containsOnly(true);
    assertThat(xp(fixture.userId())).isEqualTo(20);
    assertThat(dsl.fetchCount(dsl.selectFrom("lesson_progress"))).isEqualTo(1);
  }

  @Test
  void retryPreservesCompletionTimestampAndDoesNotAwardAgain() {
    Fixture fixture = createFixture(1);
    SaveLessonRequest request = new SaveLessonRequest(fixture.lessonIds().get(0), fixture.userId());

    saveLessonProgress.execute(request);
    var firstTimestamp = dsl.fetchValue(
        "SELECT completed_at FROM lesson_progress WHERE user_id = ? AND lesson_id = ?",
        fixture.userId(), fixture.lessonIds().get(0));
    saveLessonProgress.execute(request);

    assertThat(xp(fixture.userId())).isEqualTo(20);
    assertThat(dsl.fetchValue(
        "SELECT completed_at FROM lesson_progress WHERE user_id = ? AND lesson_id = ?",
        fixture.userId(), fixture.lessonIds().get(0))).isEqualTo(firstTimestamp);
  }

  @Test
  void existingCompletedLessonIsNotRewardedAfterUpgrade() {
    Fixture fixture = createFixture(1);
    dsl.execute("INSERT INTO gamification_stats (user_id, xp, level, streak_days) VALUES (?, 0, 1, 0)",
        fixture.userId());
    dsl.execute("INSERT INTO lesson_progress (user_id, lesson_id, is_completed, completed_at) "
            + "VALUES (?, ?, true, TIMESTAMP '2026-01-01 00:00:00')",
        fixture.userId(), fixture.lessonIds().get(0));

    saveLessonProgress.execute(new SaveLessonRequest(fixture.lessonIds().get(0), fixture.userId()));

    assertThat(xp(fixture.userId())).isZero();
    assertThat(dsl.fetchValue(
        "SELECT completed_at::text FROM lesson_progress WHERE user_id = ? AND lesson_id = ?",
        fixture.userId(), fixture.lessonIds().get(0))).isEqualTo("2026-01-01 00:00:00");
  }

  @Test
  void simultaneousDifferentLessonsKeepAllXpAndFinalProgress() throws Exception {
    Fixture fixture = createFixture(2);

    concurrentlyTasks(List.of(
        () -> saveLessonProgress.execute(new SaveLessonRequest(fixture.lessonIds().get(0), fixture.userId())),
        () -> saveLessonProgress.execute(new SaveLessonRequest(fixture.lessonIds().get(1), fixture.userId()))));

    assertThat(xp(fixture.userId())).isEqualTo(40);
    assertThat(dsl.fetchOne(
        "SELECT progress_percent FROM enrollments WHERE user_id = ? AND course_id = ?",
        fixture.userId(), fixture.courseId()).get(0, Double.class)).isEqualTo(100.0);
  }

  @Test
  void oneHundredConcurrentDailyCheckinsAwardTenXpOnce() throws Exception {
    UUID userId = createUser();

    concurrently(100, () -> dailyCheckin.execute(userId).xp());

    assertThat(xp(userId)).isEqualTo(10);
    assertThat(dsl.fetchValue(
        "SELECT last_login_xp_date::text FROM gamification_stats WHERE user_id = ?", userId))
        .isEqualTo("2026-09-24");
  }

  @Test
  void fixedClockAwardsOnNextLocalDayButNeverTwicePerDay() {
    UUID userId = createUser();
    dailyCheckin.execute(userId);
    dailyCheckin.execute(userId);
    assertThat(xp(userId)).isEqualTo(10);

    clock.setInstant(Instant.parse("2026-09-24T17:01:00Z")); // 2026-09-25 in configured zone
    dailyCheckin.execute(userId);
    dailyCheckin.execute(userId);
    assertThat(xp(userId)).isEqualTo(20);
  }

  @Test
  void futureDatedCheckinRecordNeverAwardsOrMovesBackward() {
    UUID userId = createUser();
    dsl.execute("INSERT INTO gamification_stats "
            + "(user_id, xp, level, streak_days, last_login_xp_date) VALUES (?, 0, 1, 0, DATE '2026-09-25')",
        userId);

    dailyCheckin.execute(userId);

    assertThat(xp(userId)).isZero();
    assertThat(dsl.fetchValue(
        "SELECT last_login_xp_date::text FROM gamification_stats WHERE user_id = ?", userId))
        .isEqualTo("2026-09-25");
  }

  @Test
  void loginAndExplicitCheckinShareOneDailyReward() {
    UUID userId = UUID.randomUUID();
    String email = userId + "@example.test";
    dsl.execute("INSERT INTO users (user_id, email, password_hash, full_name, role) VALUES (?, ?, ?, ?, ?)",
        userId, email, passwordEncoder.encode("secret"), "Login User", "user");
    when(refreshTokenService.createRefreshToken(userId)).thenReturn("refresh-token");

    assertThat(logIn.execute(new LogInRequest(email, "secret")).success()).isTrue();
    dailyCheckin.execute(userId);

    assertThat(xp(userId)).isEqualTo(10);
  }

  @Test
  void concurrentFirstStatsAccessCreatesOneRow() throws Exception {
    UUID userId = createUser();

    concurrently(100, () -> getStats.execute(userId).id());

    assertThat(dsl.fetchCount(dsl.selectFrom("gamification_stats"))).isEqualTo(1);
  }

  @Test
  void failedTransactionRollsBackProgressAndXpThenRetrySucceedsOnce() {
    Fixture fixture = createFixture(1);

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> rollbackProbe.writeThenFail(
            fixture.userId(), fixture.courseId(), fixture.lessonIds().get(0)))
        .isInstanceOf(IllegalStateException.class);
    assertThat(dsl.fetchCount(dsl.selectFrom("lesson_progress"))).isZero();
    assertThat(statsRepository.findByUserId(fixture.userId())).isNull();
    assertThat(dsl.fetchValue(
        "SELECT is_completed FROM enrollments WHERE user_id = ? AND course_id = ?",
        fixture.userId(), fixture.courseId())).isEqualTo(false);
    assertThat(dsl.fetchValue("SELECT count(*) FROM badges WHERE condition_type = ?",
        "COURSE_" + fixture.courseId())).isEqualTo(0L);

    saveLessonProgress.execute(new SaveLessonRequest(fixture.lessonIds().get(0), fixture.userId()));
    assertThat(xp(fixture.userId())).isEqualTo(20);
    assertThat(dsl.fetchCount(dsl.selectFrom("lesson_progress"))).isEqualTo(1);
  }

  @Test
  void concurrentFirstQuizPassesAwardQuizAndCourseXpOnce() throws Exception {
    QuizFixture fixture = createQuizFixture();
    SubmitQuizRequest request = new SubmitQuizRequest(
        fixture.userId(), fixture.quizLessonId(),
        List.of(new AnswerItem(fixture.questionId(), fixture.correctOptionId())));

    List<Callable<SubmitQuizResponse>> tasks = List.of(
        () -> submitQuiz.execute(request),
        () -> submitQuiz.execute(request));
    List<SubmitQuizResponse> responses = concurrentlyTasks(tasks);

    assertThat(responses).extracting(r -> r.xpRewarded()).containsExactlyInAnyOrder(80, 0);
    assertThat(xp(fixture.userId())).isEqualTo(80);
    assertThat(dsl.fetchCount(dsl.selectFrom("quiz_attempts"))).isEqualTo(2);
    assertThat(dsl.fetchCount(dsl.selectFrom("lesson_progress"))).isEqualTo(2);
  }

  @Test
  void simultaneousContentAndQuizCompletionKeepsAllRewardsAndProgress() throws Exception {
    QuizFixture quiz = createQuizFixture();
    UUID secondLesson = UUID.randomUUID();
    dsl.execute("INSERT INTO lesson (lesson_id, course_id, title, content_type) VALUES (?, ?, ?, 'text')",
        secondLesson, quiz.courseId(), "Second lesson");
    SubmitQuizRequest request = new SubmitQuizRequest(
        quiz.userId(), quiz.quizLessonId(),
        List.of(new AnswerItem(quiz.questionId(), quiz.correctOptionId())));

    concurrentlyTasks(List.of(
        () -> submitQuiz.execute(request),
        () -> saveLessonProgress.execute(new SaveLessonRequest(secondLesson, quiz.userId()))));

    assertThat(xp(quiz.userId())).isEqualTo(100);
    assertThat(dsl.fetchOne(
        "SELECT progress_percent FROM enrollments WHERE user_id = ? AND course_id = ?",
        quiz.userId(), quiz.courseId()).get(0, Double.class)).isEqualTo(100.0);
  }

  @Test
  void contentCompletionAfterQuizPassAwardsCourseXpOnce() {
    QuizFixture quiz = createQuizFixture();
    UUID finalLesson = UUID.randomUUID();
    dsl.execute("INSERT INTO lesson (lesson_id, course_id, title, content_type) VALUES (?, ?, ?, 'text')",
        finalLesson, quiz.courseId(), "Final lesson");
    SubmitQuizRequest request = new SubmitQuizRequest(
        quiz.userId(), quiz.quizLessonId(),
        List.of(new AnswerItem(quiz.questionId(), quiz.correctOptionId())));

    assertThat(submitQuiz.execute(request).xpRewarded()).isEqualTo(30);
    saveLessonProgress.execute(new SaveLessonRequest(finalLesson, quiz.userId()));
    saveLessonProgress.execute(new SaveLessonRequest(finalLesson, quiz.userId()));

    assertThat(xp(quiz.userId())).isEqualTo(100);
    assertThat(dsl.fetchOne(
        "SELECT progress_percent FROM enrollments WHERE user_id = ? AND course_id = ?",
        quiz.userId(), quiz.courseId()).get(0, Double.class)).isEqualTo(100.0);
  }

  @Test
  void twoLearnersCompletingSameCourseCreateOneBadgeAndTwoAwards() throws Exception {
    UUID courseId = UUID.randomUUID();
    UUID lessonId = UUID.randomUUID();
    UUID firstUser = createUser();
    UUID secondUser = createUser();
    dsl.execute("INSERT INTO courses (course_id, title, status) VALUES (?, ?, 'active')",
        courseId, "Shared course");
    dsl.execute("INSERT INTO lesson (lesson_id, course_id, title, content_type) VALUES (?, ?, 'Only lesson', 'text')",
        lessonId, courseId);
    dsl.execute("INSERT INTO enrollments (user_id, course_id) VALUES (?, ?), (?, ?)",
        firstUser, courseId, secondUser, courseId);

    concurrentlyTasks(List.of(
        () -> saveLessonProgress.execute(new SaveLessonRequest(lessonId, firstUser)),
        () -> saveLessonProgress.execute(new SaveLessonRequest(lessonId, secondUser))));

    String condition = "COURSE_" + courseId;
    assertThat(dsl.fetchValue("SELECT count(*) FROM badges WHERE condition_type = ?", condition))
        .isEqualTo(1L);
    assertThat(dsl.fetchValue(
        "SELECT count(*) FROM user_badges ub JOIN badges b ON b.id = ub.badge_id "
            + "WHERE b.condition_type = ?", condition)).isEqualTo(2L);
  }

  @Test
  void formEndpointPreservesContractAndRejectsWrongOwner() throws Exception {
    Fixture fixture = createFixture(1);
    UUID attacker = createUser();
    var ownerAuthentication = auth(fixture.userId(), "ROLE_USER");
    var attackerAuthentication = auth(attacker, "ROLE_USER");

    mockMvc.perform(post("/api/progress/lesson")
            .with(authentication(ownerAuthentication))
            .param("lessonId", fixture.lessonIds().get(0).toString())
            .param("userId", fixture.userId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    mockMvc.perform(post("/api/progress/lesson")
            .with(authentication(attackerAuthentication))
            .param("lessonId", fixture.lessonIds().get(0).toString())
            .param("userId", fixture.userId().toString()))
        .andExpect(status().isForbidden());
  }

  @Test
  void learnerCannotAssignXpAndUnauthenticatedMutationIsRejected() throws Exception {
    UUID userId = createUser();

    mockMvc.perform(post("/api/users/{userId}/xp", userId)
            .with(authentication(auth(userId, "ROLE_USER")))
            .contentType("application/json")
            .content("{\"amount\":1000}"))
        .andExpect(status().isForbidden());

    mockMvc.perform(post("/api/users/{userId}/daily-checkin", userId))
        .andExpect(status().isUnauthorized());
    assertThat(statsRepository.findByUserId(userId)).isNull();
  }

  @Test
  void enrolledOwnershipIsRequiredBeforeAnyProgressMutation() throws Exception {
    Fixture fixture = createFixture(1);
    UUID unenrolledUser = createUser();

    mockMvc.perform(post("/api/progress/lesson")
            .with(authentication(auth(unenrolledUser, "ROLE_USER")))
            .param("lessonId", fixture.lessonIds().get(0).toString())
            .param("userId", unenrolledUser.toString()))
        .andExpect(status().isForbidden());

    assertThat(statsRepository.findByUserId(unenrolledUser)).isNull();
    assertThat(dsl.fetchCount(dsl.selectFrom("lesson_progress"))).isZero();
  }

  private Fixture createFixture(int lessonCount) {
    UUID userId = createUser();
    UUID courseId = UUID.randomUUID();
    dsl.execute("INSERT INTO courses (course_id, title, learning_model, status) VALUES (?, ?, ?, ?)",
        courseId, "Concurrency course " + courseId, "self-paced", "active");
    dsl.execute("INSERT INTO enrollments (user_id, course_id) VALUES (?, ?)", userId, courseId);
    List<UUID> lessons = new ArrayList<>();
    for (int i = 0; i < lessonCount; i++) {
      UUID lessonId = UUID.randomUUID();
      dsl.execute("INSERT INTO lesson (lesson_id, course_id, title, content_type) VALUES (?, ?, ?, ?)",
          lessonId, courseId, "Lesson " + i, "text");
      lessons.add(lessonId);
    }
    return new Fixture(userId, courseId, lessons);
  }

  private UUID createUser() {
    UUID userId = UUID.randomUUID();
    dsl.execute("INSERT INTO users (user_id, email, password_hash, full_name, role) VALUES (?, ?, ?, ?, ?)",
        userId, userId + "@example.test", "unused", "Integration User", "user");
    return userId;
  }

  private QuizFixture createQuizFixture() {
    Fixture base = createFixture(1);
    UUID parentLessonId = base.lessonIds().get(0);
    UUID quizLessonId = UUID.randomUUID();
    dsl.execute("INSERT INTO lesson (lesson_id, course_id, title, content_type, parent_lesson_id) "
            + "VALUES (?, ?, 'Quiz', 'quiz', ?)",
        quizLessonId, base.courseId(), parentLessonId);
    Long questionId = dsl.fetchOne(
        "INSERT INTO questions (lesson_id, question_text, points) VALUES (?, 'Correct?', 10) "
            + "RETURNING question_id", quizLessonId).get(0, Long.class);
    Long optionId = dsl.fetchOne(
        "INSERT INTO question_options (question_id, option_text, is_correct) VALUES (?, 'Yes', true) "
            + "RETURNING option_id", questionId).get(0, Long.class);
    return new QuizFixture(base.userId(), base.courseId(), parentLessonId,
        quizLessonId, questionId, optionId);
  }

  private int xp(UUID userId) {
    return dsl.fetchOne("SELECT xp FROM gamification_stats WHERE user_id = ?", userId)
        .get(0, Integer.class);
  }

  private UsernamePasswordAuthenticationToken auth(UUID userId, String role) {
    return new UsernamePasswordAuthenticationToken(
        userId, null, List.of(new SimpleGrantedAuthority(role)));
  }

  private <T> List<T> concurrently(int calls, Callable<T> task) throws Exception {
    List<Callable<T>> tasks = new ArrayList<>();
    for (int i = 0; i < calls; i++) tasks.add(task);
    return concurrentlyTasks(tasks);
  }

  private <T> List<T> concurrentlyTasks(List<Callable<T>> tasks) throws Exception {
    CountDownLatch ready = new CountDownLatch(tasks.size());
    CountDownLatch start = new CountDownLatch(1);
    var executor = Executors.newFixedThreadPool(tasks.size());
    try {
      List<Future<T>> futures = new ArrayList<>();
      for (Callable<T> task : tasks) {
        futures.add(executor.submit(() -> {
          ready.countDown();
          if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("start timeout");
          return task.call();
        }));
      }
      assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
      start.countDown();
      List<T> results = new ArrayList<>();
      for (Future<T> future : futures) results.add(future.get(60, TimeUnit.SECONDS));
      return results;
    } finally {
      executor.shutdownNow();
      assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
    }
  }

  private record Fixture(UUID userId, UUID courseId, List<UUID> lessonIds) {
  }

  private record QuizFixture(UUID userId, UUID courseId, UUID parentLessonId,
      UUID quizLessonId, Long questionId, Long correctOptionId) {
  }
}
