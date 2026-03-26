package com.eduflex.entity;

import java.util.UUID;

import com.eduflex.generated.tables.Lesson;
import com.eduflex.generated.tables.records.LessonRecord;

public class LessonDbO {
  public LessonRecord record;

  public LessonDbO(UUID courseID, String title, String contentType) {
    record = Lesson.LESSON.newRecord();
    record.setCourseId(courseID);
    record.setTitle(title);
    record.setContentType(contentType);
  }
}
