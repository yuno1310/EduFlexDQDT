package com.eduflex.service;

import com.eduflex.dto.QuizDTO.SubmitQuizRequest;
import com.eduflex.dto.QuizDTO.SubmitQuizResponse;
import com.eduflex.repository.LessonProgressRepository;
import com.eduflex.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SubmitQuizUseCase {

  @Autowired
  private QuizRepository quizRepository;

  @Autowired
  private LessonProgressRepository progressRepository;

  @Transactional
  public SubmitQuizResponse execute(SubmitQuizRequest request) {
    UUID userId = request.userId();
    UUID lessonId = request.lessonId();
    Long optionId = request.selectedOptionId();

    boolean isCorrect = quizRepository.isAnswerCorrect(optionId);
    Double score = isCorrect ? 100.0d : 0.0d;

    quizRepository.saveQuizAttempt(userId, lessonId, score, isCorrect);

    if (!isCorrect) {
      return new SubmitQuizResponse(false, "Sai mất rồi bro ơi, thử lại nhé!", 0.0, false, 0);
    }

    progressRepository.upsertLessonProgress(userId, lessonId);

    UUID courseId = progressRepository.getCourseIdByLessonId(lessonId);
    if (courseId == null) {
      return new SubmitQuizResponse(true, "Đúng rồi! Nhưng không tìm thấy khóa học.", 0.0, false, 0);
    }

    int totalLessons = progressRepository.countTotalLessonsInCourse(courseId);
    int completedLessons = progressRepository.countCompletedLessons(userId, courseId);

    double percent = (totalLessons == 0) ? 0.0 : ((double) completedLessons / totalLessons) * 100;
    percent = Math.round(percent * 10.0) / 10.0;

    progressRepository.updateCourseProgress(userId, courseId, percent);

    boolean isCourseCompleted = (completedLessons == totalLessons && totalLessons > 0);
    int xpRewarded = 0;
    String msg = "Chính xác! Đã cập nhật tiến độ.";

    if (isCourseCompleted) {
      xpRewarded = 50;
      quizRepository.rewardXP(userId, xpRewarded);
      msg = "Đỉnh quá! Ban đã hoàn thành khóa học và nhận được " + xpRewarded + " XP.";
    }

    return new SubmitQuizResponse(true, msg, percent, isCourseCompleted, xpRewarded);
  }
}
