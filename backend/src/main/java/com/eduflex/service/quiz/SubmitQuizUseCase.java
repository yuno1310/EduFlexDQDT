package com.eduflex.service.quiz;

import com.eduflex.dto.gamification.AddXpDTO;
import com.eduflex.dto.quiz.QuizDTO.AnswerItem;
import com.eduflex.dto.quiz.QuizDTO.CompletedQuestInfo;
import com.eduflex.dto.quiz.QuizDTO.SubmitQuizRequest;
import com.eduflex.dto.quiz.QuizDTO.SubmitQuizResponse;
import com.eduflex.repository.enrollment.EnrollmentRepository;
import com.eduflex.repository.lesson.LessonProgressRepository;
import com.eduflex.repository.quiz.QuizRepository;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SubmitQuizUseCase {

  private static final double PASS_PERCENTAGE_THRESHOLD = 50.0;
  private static final int QUIZ_PASS_XP = 30;
  private static final int COURSE_COMPLETE_XP = 50;
  private final RabbitTemplate rabbitTemplate;

  @Autowired
  private QuizRepository quizRepository;
  @Autowired
  private LessonProgressRepository progressRepository;
  @Autowired
  private AddXpUseCase addXpUseCase;
  @Autowired
  private UpdateStreakUseCase updateStreakUseCase;
  @Autowired
  private EnrollmentRepository enrollmentRepository;
  @Autowired
  private CheckAndAwardBadgesUseCase checkAndAwardBadgesUseCase;
  @Autowired
  private UpdateDailyQuestProgressUseCase updateDailyQuestProgressUseCase;

  public SubmitQuizUseCase(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }


  @Transactional
  public SubmitQuizResponse execute(SubmitQuizRequest request) {
    UUID userId = request.userId();
    UUID lessonId = request.lessonId();

    // 1. Grade the quiz (Calculate correct count and percent)
    int correctCount = 0;
    for (AnswerItem answer : request.answers()) {
      if (quizRepository.isAnswerCorrect(answer.selectedOptionId())) {
        correctCount++;
      }
    }

    int totalQuestions = request.answers().size();
    double scorePercent = totalQuestions == 0 ? 0.0 : ((double) correctCount / totalQuestions) * 100;
    scorePercent = Math.round(scorePercent * 10.0) / 10.0;

    // New Pass condition: Compare score % with threshold
    boolean passed = scorePercent >= PASS_PERCENTAGE_THRESHOLD;

    // 2. Save quiz attempt (Must save regardless of pass/fail or attempt number)
    boolean alreadyPassed = quizRepository.hasPassedQuiz(userId, lessonId);
    quizRepository.saveQuizAttempt(userId, lessonId, scorePercent, passed);

    // Daily quest: count every quiz submission
    List<CompletedQuestInfo> completedQuests = new ArrayList<>();
    CompletedQuestInfo quizCountCompleted = updateDailyQuestProgressUseCase.execute(userId, "QUIZ_COUNT", 1);
    if (quizCountCompleted != null) completedQuests.add(quizCountCompleted);
    // Daily quest: perfect run — increment on 100%, reset on any wrong answer
    boolean isPerfect = correctCount == totalQuestions && totalQuestions > 0;
    CompletedQuestInfo perfectRunCompleted = updateDailyQuestProgressUseCase.execute(userId, "PERFECT_RUN", isPerfect ? 1 : -1);
    if (perfectRunCompleted != null) completedQuests.add(perfectRunCompleted);

    // 3. Handle FAILED case
    if (!passed) {
      return new SubmitQuizResponse(
          false,
          "You answered correctly " + correctCount + "/" + totalQuestions + ". Keep trying to pass this lesson!",
          correctCount, totalQuestions, scorePercent,
          null, false, 0, completedQuests);
    }

    // 4. Handle ALREADY PASSED case (Prevent XP farming and redundant DB queries)
    if (alreadyPassed) {
      return new SubmitQuizResponse(
          true,
          "Great job! You got " + correctCount + "/" + totalQuestions
              + " correct. (Note: No extra XP awarded for previously passed quizzes)",
          correctCount, totalQuestions, scorePercent,
          null, false, 0, completedQuests);
    }

    int totalXpRewarded = QUIZ_PASS_XP;

    // 5. Award Quiz XP & Update Streak
    addXpUseCase.execute(userId, new AddXpDTO.AddXpRequest(QUIZ_PASS_XP));
    updateStreakUseCase.execute(userId);

    // 6. Mark quiz lesson as completed
    progressRepository.upsertLessonProgress(userId, lessonId);

    // 6b. Also mark the PARENT (content) lesson as completed
    UUID parentLessonId = quizRepository.getParentLessonId(lessonId);
    if (parentLessonId != null) {
      progressRepository.upsertLessonProgress(userId, parentLessonId);
    }

    // 7. Calculate Course Progress
    UUID courseId = progressRepository.getCourseIdByLessonId(lessonId);
    if (courseId == null) {
      return new SubmitQuizResponse(
          true, "Lesson passed! (+" + totalXpRewarded + " XP)",
          correctCount, totalQuestions, scorePercent,
          null, false, totalXpRewarded, completedQuests);
    }

    int totalLessons = progressRepository.countTotalLessonsInCourse(courseId);
    int completedLessons = progressRepository.countCompletedLessons(userId, courseId);

    double coursePercent = (totalLessons == 0) ? 0.0 : ((double) completedLessons / totalLessons) * 100;
    coursePercent = Math.round(coursePercent * 10.0) / 10.0;
    progressRepository.updateCourseProgress(userId, courseId, coursePercent);

    // 8. Final check: Is the course 100% completed?
    boolean isCourseCompleted = (completedLessons == totalLessons && totalLessons > 0);
    String msg = "Congratulations, you passed the lesson! (+" + QUIZ_PASS_XP + " XP)";

    if (isCourseCompleted) {
      totalXpRewarded += COURSE_COMPLETE_XP;
      addXpUseCase.execute(userId, new AddXpDTO.AddXpRequest(COURSE_COMPLETE_XP));
      enrollmentRepository.markCourseAsCompleted(userId, courseId);
      checkAndAwardBadgesUseCase.checkCourseCompletionBadge(userId, courseId);
      msg = "Awesome! You have completed 100% of the course! (+" + totalXpRewarded + " XP)";
    }

    return new SubmitQuizResponse(
        true, msg, correctCount, totalQuestions, scorePercent,
        coursePercent, isCourseCompleted, totalXpRewarded, completedQuests);
  }
}
