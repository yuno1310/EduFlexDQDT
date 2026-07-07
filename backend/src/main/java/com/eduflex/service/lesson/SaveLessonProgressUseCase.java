package com.eduflex.service.lesson;

import com.eduflex.dto.gamification.AddXpDTO;
import com.eduflex.dto.lesson.ProgressDTO.SaveLessonRequest;
import com.eduflex.dto.lesson.ProgressDTO.SaveLessonResponse;
import com.eduflex.repository.lesson.LessonProgressRepository;
import com.eduflex.repository.enrollment.EnrollmentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SaveLessonProgressUseCase {

  private static final int LESSON_COMPLETE_XP = 20;

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

  @Transactional
  public SaveLessonResponse execute(SaveLessonRequest request) {
    try {
      UUID userId = request.userId();
      UUID lessonId = request.lessonId();

      // Check if already completed (to avoid duplicate XP)
      boolean alreadyCompleted = progressRepository.isLessonCompleted(userId, lessonId);

      progressRepository.upsertLessonProgress(userId, lessonId);

      // Award XP only on first completion
      if (!alreadyCompleted) {
        addXpUseCase.execute(userId, new AddXpDTO.AddXpRequest(LESSON_COMPLETE_XP));
      }

      // Update streak (studying today counts as activity)
      updateStreakUseCase.execute(userId);

      UUID courseId = progressRepository.getCourseIdByLessonId(lessonId);
      if (courseId == null) {
        return new SaveLessonResponse(false, "Không tìm thấy khóa học chứa bài này", 0.0);
      }
      int totalLessons = progressRepository.countTotalLessonsInCourse(courseId);
      int completedLessons = progressRepository.countCompletedLessons(userId, courseId);
      double percent = totalLessons == 0 ? 0.0 : ((double) completedLessons / totalLessons) * 100;
      percent = Math.round(percent * 10.0) / 10.0;
      progressRepository.updateCourseProgress(userId, courseId, percent);

      // Check course completion
      boolean isCourseCompleted = (completedLessons == totalLessons && totalLessons > 0);
      if (isCourseCompleted) {
        enrollmentRepository.markCourseAsCompleted(userId, courseId);
        checkAndAwardBadgesUseCase.checkCourseCompletionBadge(userId, courseId);
      }

      String xpMsg = alreadyCompleted ? "" : " (+" + LESSON_COMPLETE_XP + " XP)";
      if (isCourseCompleted) {
        xpMsg += " - Course Completed!";
      }

      return new SaveLessonResponse(true, "Lưu tiến độ thành công!" + xpMsg, percent);

    } catch (Exception e) {
      e.printStackTrace();
      return new SaveLessonResponse(false, "Lỗi server: " + e.getMessage(), 0.0);
    }
  }
}
