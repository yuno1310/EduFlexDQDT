package com.eduflex.service.lesson;

import com.eduflex.dto.gamification.AddXpDTO;
import com.eduflex.dto.lesson.ProgressDTO.SaveLessonRequest;
import com.eduflex.dto.lesson.ProgressDTO.SaveLessonResponse;
import com.eduflex.repository.lesson.LessonProgressRepository;
import com.eduflex.repository.enrollment.EnrollmentRepository;
import com.eduflex.exception.ResourceNotFoundException;
import com.eduflex.repository.gamification.GamificationStatsRepository;
import com.eduflex.repository.quiz.QuizRepository;
import com.eduflex.service.gamification.AddXpUseCase;
import com.eduflex.service.gamification.CheckAndAwardBadgesUseCase;
import com.eduflex.service.gamification.UpdateStreakUseCase;
import com.eduflex.monitoring.EduFlexMetrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SaveLessonProgressUseCase {

  private static final int LESSON_COMPLETE_XP = 20;
  private static final int COURSE_COMPLETE_XP = 50;

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
  private GamificationStatsRepository gamificationStatsRepository;

  @Autowired
  private QuizRepository quizRepository;

  @Autowired
  private EduFlexMetrics metrics;

  @Transactional
  public SaveLessonResponse execute(SaveLessonRequest request) {
    UUID userId = request.userId();
    UUID lessonId = request.lessonId();
    gamificationStatsRepository.ensureAndLock(userId);

    UUID courseId = progressRepository.getCourseIdByLessonId(lessonId);
    if (courseId == null) {
      throw new ResourceNotFoundException("Lesson not found: " + lessonId);
    }
    if (!enrollmentRepository.isUserEnrolled(userId, courseId)) {
      throw new AccessDeniedException("You must be enrolled before completing this lesson");
    }
    if (progressRepository.isQuizLesson(lessonId)) {
      throw new IllegalArgumentException("Quiz lessons must be completed by passing the quiz");
    }

    boolean newlyCompleted = progressRepository.completeLessonIfNeeded(userId, lessonId);
    metrics.progressCompletion(newlyCompleted);
    if (newlyCompleted) {
      addXpUseCase.execute(userId, new AddXpDTO.AddXpRequest(LESSON_COMPLETE_XP));
      metrics.reward("lesson");
    }

    updateStreakUseCase.execute(userId);

    int totalLessons = progressRepository.countTotalLessonsInCourse(courseId);
    int completedLessons = progressRepository.countCompletedLessons(userId, courseId);
    double percent = totalLessons == 0 ? 0.0 : ((double) completedLessons / totalLessons) * 100;
    percent = Math.round(percent * 10.0) / 10.0;
    progressRepository.updateCourseProgress(userId, courseId, percent);

    boolean complete = completedLessons == totalLessons && totalLessons > 0;
    boolean newlyCompletedCourse = complete
        && enrollmentRepository.markCourseAsCompletedIfNeeded(userId, courseId);
    int totalXpRewarded = newlyCompleted ? LESSON_COMPLETE_XP : 0;
    if (newlyCompletedCourse) {
      // A quiz pass may commit just before this content completion. Award the
      // course bonus to the transaction that wins the one-time course transition
      // so the result does not depend on thread scheduling.
      if (quizRepository.hasPassedQuizInCourse(userId, courseId)) {
        addXpUseCase.execute(userId, new AddXpDTO.AddXpRequest(COURSE_COMPLETE_XP));
        metrics.reward("course");
        totalXpRewarded += COURSE_COMPLETE_XP;
      }
      checkAndAwardBadgesUseCase.checkCourseCompletionBadge(userId, courseId);
    }

    String message = newlyCompleted
        ? "Lưu tiến độ thành công! (+" + totalXpRewarded + " XP)"
        : "Bài học đã được hoàn thành trước đó.";
    if (newlyCompletedCourse) {
      message += " - Course Completed!";
    }
    return new SaveLessonResponse(true, message, percent);
  }
}
