package com.eduflex.repository.lesson;

import com.eduflex.generated.tables.Enrollments;
import com.eduflex.generated.tables.Lesson;
import com.eduflex.generated.tables.LessonProgress;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class LessonProgressRepository {

  @Autowired
  private DSLContext dsl;

  public boolean completeLessonIfNeeded(UUID userId, UUID lessonId) {
    return dsl.fetchOne(
        "INSERT INTO lesson_progress (user_id, lesson_id, is_completed, completed_at) "
            + "VALUES (?, ?, true, ?) "
            + "ON CONFLICT (user_id, lesson_id) DO UPDATE "
            + "SET is_completed = true, completed_at = EXCLUDED.completed_at "
            + "WHERE lesson_progress.is_completed IS DISTINCT FROM true "
            + "RETURNING progress_id",
        userId, lessonId, LocalDateTime.now()) != null;
  }

  public UUID getCourseIdByLessonId(UUID lessonId) {
    return dsl.select(Lesson.LESSON.COURSE_ID)
        .from(Lesson.LESSON)
        .where(Lesson.LESSON.LESSON_ID.eq(lessonId))
        .fetchOneInto(UUID.class);
  }

  public boolean isQuizLesson(UUID lessonId) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(Lesson.LESSON)
            .where(Lesson.LESSON.LESSON_ID.eq(lessonId))
            .and(Lesson.LESSON.CONTENT_TYPE.equalIgnoreCase("quiz")))
        || dsl.fetchExists(
            dsl.selectOne()
                .from(com.eduflex.generated.tables.Questions.QUESTIONS)
                .where(com.eduflex.generated.tables.Questions.QUESTIONS.LESSON_ID.eq(lessonId)));
  }

  public int countTotalLessonsInCourse(UUID courseId) {
    return dsl.fetchCount(
        dsl.selectFrom(Lesson.LESSON)
            .where(Lesson.LESSON.COURSE_ID.eq(courseId))
            .and(Lesson.LESSON.PARENT_LESSON_ID.isNull()));
  }

  public int countCompletedLessons(UUID userId, UUID courseId) {
    return dsl.fetchCount(
        dsl.select()
            .from(LessonProgress.LESSON_PROGRESS)
            .join(Lesson.LESSON).on(LessonProgress.LESSON_PROGRESS.LESSON_ID.eq(Lesson.LESSON.LESSON_ID))
            .where(LessonProgress.LESSON_PROGRESS.USER_ID.eq(userId))
            .and(Lesson.LESSON.COURSE_ID.eq(courseId))
            .and(Lesson.LESSON.PARENT_LESSON_ID.isNull())
            .and(LessonProgress.LESSON_PROGRESS.IS_COMPLETED.isTrue()));
  }

  public void updateCourseProgress(UUID userId, UUID courseId, double percent) {
    dsl.update(Enrollments.ENROLLMENTS)
        .set(Enrollments.ENROLLMENTS.PROGRESS_PERCENT, percent)
        .where(Enrollments.ENROLLMENTS.USER_ID.eq(userId))
        .and(Enrollments.ENROLLMENTS.COURSE_ID.eq(courseId))
        .execute();
  }
}
