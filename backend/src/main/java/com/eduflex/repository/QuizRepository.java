package com.eduflex.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.generated.tables.LearnerProfile;
import com.eduflex.generated.tables.QuestionOptions;
import com.eduflex.generated.tables.QuizAttempts;

@Repository
public class QuizRepository {

  @Autowired
  private DSLContext dsl;

  public boolean isAnswerCorrect(Long optionId) {
    Boolean isCorrect = dsl.select(QuestionOptions.QUESTION_OPTIONS.IS_CORRECT)
        .from(QuestionOptions.QUESTION_OPTIONS)
        .where(QuestionOptions.QUESTION_OPTIONS.OPTION_ID.eq(optionId))
        .fetchOneInto(Boolean.class);
    return isCorrect != null && isCorrect;
  }

  public void saveQuizAttempt(UUID userId, UUID lessonId, Double score, boolean isPassed) {
    dsl.insertInto(QuizAttempts.QUIZ_ATTEMPTS,
        QuizAttempts.QUIZ_ATTEMPTS.USER_ID,
        QuizAttempts.QUIZ_ATTEMPTS.LESSON_ID,
        QuizAttempts.QUIZ_ATTEMPTS.SCORE,
        QuizAttempts.QUIZ_ATTEMPTS.IS_PASSED,
        QuizAttempts.QUIZ_ATTEMPTS.ATTEMPTED_AT)
        .values(userId, lessonId, score, isPassed, LocalDateTime.now())
        .execute();
  }

  public void rewardXP(UUID userId, int xpAmount) {
    dsl.update(LearnerProfile.LEARNER_PROFILE)
        .set(LearnerProfile.LEARNER_PROFILE.TOTAL_XP, LearnerProfile.LEARNER_PROFILE.TOTAL_XP.add(xpAmount))
        .where(LearnerProfile.LEARNER_PROFILE.LEARNER_ID.eq(userId))
        .execute();
  }
}
