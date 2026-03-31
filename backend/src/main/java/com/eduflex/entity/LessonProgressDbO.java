package com.eduflex.entity;

import com.eduflex.generated.tables.LessonProgress;
import com.eduflex.generated.tables.records.LessonProgressRecord;
import java.util.UUID;
import java.time.LocalDateTime;

public class LessonProgressDbO {
  public LessonProgressRecord record;

  public LessonProgressDbO(UUID userId, UUID lessonId, Boolean isCompleted) {
    record = LessonProgress.LESSON_PROGRESS.newRecord();
    record.setUserId(userId);
    record.setLessonId(lessonId);
    record.setIsCompleted(isCompleted);
    record.setCompletedAt(isCompleted ? LocalDateTime.now() : null);
  }

  public LessonProgressDbO(LessonProgressRecord record) {
    this.record = record;
  }
}
