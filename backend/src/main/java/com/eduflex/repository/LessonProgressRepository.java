package com.eduflex.repository;

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

  /**
   * Check if lesson is already completed (for duplicate XP prevention).
   */
  public boolean isLessonCompleted(UUID userId, UUID lessonId) {
    return dsl.fetchExists(
        dsl.selectFrom(LessonProgress.LESSON_PROGRESS)
            .where(LessonProgress.LESSON_PROGRESS.USER_ID.eq(userId))
            .and(LessonProgress.LESSON_PROGRESS.LESSON_ID.eq(lessonId))
            .and(LessonProgress.LESSON_PROGRESS.IS_COMPLETED.isTrue()));
  }

  public void upsertLessonProgress(UUID userId, UUID lessonId) {
    dsl.insertInto(LessonProgress.LESSON_PROGRESS,
        LessonProgress.LESSON_PROGRESS.USER_ID,
        LessonProgress.LESSON_PROGRESS.LESSON_ID,
        LessonProgress.LESSON_PROGRESS.IS_COMPLETED,
        LessonProgress.LESSON_PROGRESS.COMPLETED_AT)
        .values(userId, lessonId, true, LocalDateTime.now())
        .onConflict(LessonProgress.LESSON_PROGRESS.USER_ID, LessonProgress.LESSON_PROGRESS.LESSON_ID)
        .doUpdate()
        .set(LessonProgress.LESSON_PROGRESS.IS_COMPLETED, true)
        .set(LessonProgress.LESSON_PROGRESS.COMPLETED_AT, LocalDateTime.now())
        .execute();
  }

  public UUID getCourseIdByLessonId(UUID lessonId) {
    return dsl.select(Lesson.LESSON.COURSE_ID)
        .from(Lesson.LESSON)
        .where(Lesson.LESSON.LESSON_ID.eq(lessonId))
        .fetchOneInto(UUID.class);
  }

  public int countTotalLessonsInCourse(UUID courseId) {
    return dsl.fetchCount(
        dsl.selectFrom(Lesson.LESSON)
            .where(Lesson.LESSON.COURSE_ID.eq(courseId)));
  }

  public int countCompletedLessons(UUID userId, UUID courseId) {
    return dsl.fetchCount(
        dsl.select()
            .from(LessonProgress.LESSON_PROGRESS)
            .join(Lesson.LESSON).on(LessonProgress.LESSON_PROGRESS.LESSON_ID.eq(Lesson.LESSON.LESSON_ID))
            .where(LessonProgress.LESSON_PROGRESS.USER_ID.eq(userId))
            .and(Lesson.LESSON.COURSE_ID.eq(courseId))
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
