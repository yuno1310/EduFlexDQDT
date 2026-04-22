package com.eduflex.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.entity.QuestionDbO;
import com.eduflex.entity.QuestionOptionDbO;
import com.eduflex.generated.tables.GamificationStats;
import com.eduflex.generated.tables.QuestionOptions;
import com.eduflex.generated.tables.Questions;
import com.eduflex.generated.tables.QuizAttempts;
import com.eduflex.generated.tables.records.QuestionOptionsRecord;
import com.eduflex.generated.tables.records.QuestionsRecord;

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
    dsl.update(GamificationStats.GAMIFICATION_STATS)
        .set(GamificationStats.GAMIFICATION_STATS.XP, GamificationStats.GAMIFICATION_STATS.XP.add(xpAmount))
        .where(GamificationStats.GAMIFICATION_STATS.USER_ID.eq(userId))
        .execute();
  }

  public Long saveQuestionAndGetId(QuestionDbO question) {
    question.record.attach(dsl.configuration());
    question.record.store();
    return question.record.getQuestionId();
  }

  public void saveOption(QuestionOptionDbO option) {
    option.record.attach(dsl.configuration());
    option.record.store();
  }

  public QuestionsRecord getQuestionByLessonId(UUID lessonId) {
    return dsl.selectFrom(Questions.QUESTIONS)
        .where(Questions.QUESTIONS.LESSON_ID.eq(lessonId))
        .fetchOne();
  }

  public List<QuestionOptionsRecord> getOptionsByQuestionId(Long questionId) {
    return dsl.selectFrom(QuestionOptions.QUESTION_OPTIONS)
        .where(QuestionOptions.QUESTION_OPTIONS.QUESTION_ID.eq(questionId))
        .fetch();
  }

  public boolean hasPassedQuiz(UUID userId, UUID lessonId) {
    return dsl.fetchExists(
        dsl.selectFrom(QuizAttempts.QUIZ_ATTEMPTS)
            .where(QuizAttempts.QUIZ_ATTEMPTS.USER_ID.eq(userId))
            .and(QuizAttempts.QUIZ_ATTEMPTS.LESSON_ID.eq(lessonId))
            .and(QuizAttempts.QUIZ_ATTEMPTS.IS_PASSED.isTrue()));
  }

  public int countQuestionsByLessonId(UUID lessonId) {
    return dsl.selectCount()
        .from(Questions.QUESTIONS)
        .where(Questions.QUESTIONS.LESSON_ID.eq(lessonId))
        .fetchOne(0, int.class);
  }

  public List<String> getCorrectTextsForQuestion(Long questionId) {
    var records = dsl.select(QuestionOptions.QUESTION_OPTIONS.OPTION_TEXT)
        .from(QuestionOptions.QUESTION_OPTIONS)
        .where(QuestionOptions.QUESTION_OPTIONS.QUESTION_ID.eq(questionId))
        .and(QuestionOptions.QUESTION_OPTIONS.IS_CORRECT.eq(true))
        .fetch();

    if (records != null && records.isNotEmpty()) {
      List<String> list = new ArrayList<String>();
      for (var record : records) {
        list.add(record.value1());
      }
      return list;
    } else {
      return null;
    }
  }

  public boolean updateQuestion(Long questionId, String questionText, int points) {
    return dsl.update(Questions.QUESTIONS)
        .set(Questions.QUESTIONS.QUESTION_TEXT, questionText)
        .set(Questions.QUESTIONS.POINTS, points)
        .where(Questions.QUESTIONS.QUESTION_ID.eq(questionId))
        .execute() > 0;
  }

  public boolean updateOption(Long optionId, String optionText, boolean isCorrect) {
    return dsl.update(QuestionOptions.QUESTION_OPTIONS)
        .set(QuestionOptions.QUESTION_OPTIONS.OPTION_TEXT, optionText)
        .set(QuestionOptions.QUESTION_OPTIONS.IS_CORRECT, isCorrect)
        .where(QuestionOptions.QUESTION_OPTIONS.OPTION_ID.eq(optionId))
        .execute() > 0;
  }

  public boolean questionExistsById(Long questionId) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(Questions.QUESTIONS)
            .where(Questions.QUESTIONS.QUESTION_ID.eq(questionId)));
  }
}
