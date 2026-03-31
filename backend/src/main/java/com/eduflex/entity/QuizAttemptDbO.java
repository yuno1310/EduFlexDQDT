package com.eduflex.entity;

import com.eduflex.generated.tables.QuizAttempts;
import com.eduflex.generated.tables.records.QuizAttemptsRecord;
import java.util.UUID;
import java.time.LocalDateTime;

public class QuizAttemptDbO {
  public QuizAttemptsRecord record;

  public QuizAttemptDbO(UUID userId, UUID lessonId, Double score, Boolean isPassed) {
    record = QuizAttempts.QUIZ_ATTEMPTS.newRecord();
    record.setUserId(userId);
    record.setLessonId(lessonId);
    record.setScore(score);
    record.setIsPassed(isPassed);
    record.setAttemptedAt(LocalDateTime.now());
  }

  public QuizAttemptDbO(QuizAttemptsRecord record) {
    this.record = record;
  }
}
