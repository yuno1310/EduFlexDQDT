package com.eduflex.entity.lesson;

import java.util.UUID;

import com.eduflex.generated.tables.Lesson;
import com.eduflex.generated.tables.records.LessonRecord;

public class LessonDbO {
  public LessonRecord record;

  public LessonDbO(UUID courseID, String title, String contentType, String content) {
    record = Lesson.LESSON.newRecord();
    record.setCourseId(courseID);
    record.setTitle(title);
    record.setContentType(contentType);
    record.setContent(content);
  }

  public LessonDbO(UUID courseID, String title, String contentType, String videoUrl, String content, UUID parentLessonId) {
    record = Lesson.LESSON.newRecord();
    record.setCourseId(courseID);
    record.setTitle(title);
    record.setContentType(contentType);
    if (videoUrl != null) record.setVideoUrl(videoUrl);
    if (content != null) record.setContent(content);
    if (parentLessonId != null) record.setParentLessonId(parentLessonId);
  }

  public LessonDbO(LessonRecord record) {
    this.record = record;
  }
}
