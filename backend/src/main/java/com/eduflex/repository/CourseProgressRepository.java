package com.eduflex.repository;

import com.eduflex.dto.CourseProgressDTO.LessonProgressInfo;
import com.eduflex.generated.tables.Lesson;
import com.eduflex.generated.tables.LessonProgress;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class CourseProgressRepository {
  @Autowired
  private DSLContext dsl;

  public List<LessonProgressInfo> getLessonsWithProgress(UUID userId, UUID courseId) {
    var records = dsl.select(
        Lesson.LESSON.LESSON_ID,
        Lesson.LESSON.TITLE,
        Lesson.LESSON.CONTENT_TYPE,
        LessonProgress.LESSON_PROGRESS.IS_COMPLETED)
        .from(Lesson.LESSON)
        .leftJoin(LessonProgress.LESSON_PROGRESS)
        .on(Lesson.LESSON.LESSON_ID.eq(LessonProgress.LESSON_PROGRESS.LESSON_ID)
            .and(LessonProgress.LESSON_PROGRESS.USER_ID.eq(userId)))
        .where(Lesson.LESSON.COURSE_ID.eq(courseId))
        .orderBy(Lesson.LESSON.LESSON_ID.asc())
        .fetch();

    if (records != null && records.isNotEmpty()) {
      List<LessonProgressInfo> list = new ArrayList<LessonProgressInfo>();
      for (var record : records) {
        LessonProgressInfo info = new LessonProgressInfo(
            record.value1(),
            record.value2(),
            record.value3(),
            record.value4());
        list.add(info);
      }
      return list;
    } else {
      return null;
    }
  }
}
