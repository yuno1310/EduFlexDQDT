package com.eduflex.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.dto.GetLessonDTO.LessonInfo;
import com.eduflex.entity.LessonDbO;
import com.eduflex.generated.tables.Lesson;

@Repository
public class LessonRepository {
  @Autowired
  private DSLContext dsl;

  public boolean save(LessonDbO lesson) {
    lesson.record.attach(dsl.configuration());
    if (lesson.record.store() > 0) {
      return true;
    } else {
      return false;
    }
  }

  public List<LessonInfo> getLesson(UUID courseID) {
    var records = dsl.select(Lesson.LESSON.LESSON_ID, Lesson.LESSON.TITLE, Lesson.LESSON.CONTENT_TYPE)
        .from(Lesson.LESSON)
        .where(Lesson.LESSON.COURSE_ID.eq(courseID)).fetch();
    if (records != null) {
      List<LessonInfo> listLesson = new ArrayList<LessonInfo>();
      for (var record : records) {
        LessonInfo lesson = new LessonInfo(record.value1(), record.value2(), record.value3());
        listLesson.add(lesson);
      }
      return listLesson;
    } else {
      return null;
    }
  }
}
