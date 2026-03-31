package com.eduflex.service;

import com.eduflex.dto.AddXpDTO;
import com.eduflex.dto.QuizDTO.AnswerItem;
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

  private static final int PASS_THRESHOLD = 5; // Need > 5 correct out of 10 to pass
  private static final int QUIZ_PASS_XP = 30;
  private static final int COURSE_COMPLETE_XP = 50;

  @Autowired
  private QuizRepository quizRepository;

  @Autowired
  private LessonProgressRepository progressRepository;

  @Autowired
  private AddXpUseCase addXpUseCase;

  @Autowired
  private UpdateStreakUseCase updateStreakUseCase;

  @Transactional
  public SubmitQuizResponse execute(SubmitQuizRequest request) {
    UUID userId = request.userId();
    UUID lessonId = request.lessonId();

    // 1. Count correct answers
    int correctCount = 0;
    for (AnswerItem answer : request.answers()) {
      if (quizRepository.isAnswerCorrect(answer.selectedOptionId())) {
        correctCount++;
      }
    }

    int totalQuestions = request.answers().size();
    double scorePercent = totalQuestions == 0 ? 0.0 : ((double) correctCount / totalQuestions) * 100;
    scorePercent = Math.round(scorePercent * 10.0) / 10.0;
    boolean passed = correctCount > PASS_THRESHOLD;

    // 2. Check if user already passed this quiz (for duplicate XP prevention)
    boolean alreadyPassed = quizRepository.hasPassedQuiz(userId, lessonId);

    // 3. Save quiz attempt
    quizRepository.saveQuizAttempt(userId, lessonId, scorePercent, passed);

    // 4. If not passed, return result without updating progress
    if (!passed) {
      return new SubmitQuizResponse(
          false,
          "Bạn trả lời đúng " + correctCount + "/" + totalQuestions + " câu. Cần đúng trên " + PASS_THRESHOLD + " câu để pass! Thử lại nhé!",
          correctCount, totalQuestions, scorePercent,
          null, false, 0);
    }

    // 5. Award XP for passing (only on first pass)
    int xpRewarded = 0;
    if (!alreadyPassed) {
      xpRewarded = QUIZ_PASS_XP;
      addXpUseCase.execute(userId, new AddXpDTO.AddXpRequest(xpRewarded));
    }

    // 6. Update streak
    updateStreakUseCase.execute(userId);

    // 7. Mark lesson as completed
    progressRepository.upsertLessonProgress(userId, lessonId);

    // 8. Recalculate course progress
    UUID courseId = progressRepository.getCourseIdByLessonId(lessonId);
    if (courseId == null) {
      return new SubmitQuizResponse(
          true,
          "Bạn đã pass! " + correctCount + "/" + totalQuestions + " câu đúng." + (alreadyPassed ? "" : " (+" + xpRewarded + " XP)"),
          correctCount, totalQuestions, scorePercent,
          null, false, xpRewarded);
    }

    int totalLessons = progressRepository.countTotalLessonsInCourse(courseId);
    int completedLessons = progressRepository.countCompletedLessons(userId, courseId);

    double coursePercent = (totalLessons == 0) ? 0.0 : ((double) completedLessons / totalLessons) * 100;
    coursePercent = Math.round(coursePercent * 10.0) / 10.0;

    progressRepository.updateCourseProgress(userId, courseId, coursePercent);

    boolean isCourseCompleted = (completedLessons == totalLessons && totalLessons > 0);
    String msg;

    if (isCourseCompleted) {
      xpRewarded += COURSE_COMPLETE_XP;
      addXpUseCase.execute(userId, new AddXpDTO.AddXpRequest(COURSE_COMPLETE_XP));
      msg = "Đỉnh quá! Bạn đã hoàn thành khóa học! " + correctCount + "/" + totalQuestions + " câu đúng. (+" + xpRewarded + " XP)";
    } else if (alreadyPassed) {
      msg = "Bạn đã pass! " + correctCount + "/" + totalQuestions + " câu đúng. (XP đã được cộng trước đó)";
    } else {
      msg = "Bạn đã pass! " + correctCount + "/" + totalQuestions + " câu đúng. (+" + xpRewarded + " XP)";
    }

    return new SubmitQuizResponse(
        true, msg, correctCount, totalQuestions, scorePercent,
        coursePercent, isCourseCompleted, xpRewarded);
  }
}
