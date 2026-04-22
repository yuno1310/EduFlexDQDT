package com.eduflex.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.dto.EnrolledCoursesDTO.EnrolledCourseInfo;
import com.eduflex.entity.EnrollmentDbO;
import com.eduflex.generated.tables.Courses;
import com.eduflex.generated.tables.Enrollments;

@Repository
public class EnrollmentRepository {
  @Autowired
  private DSLContext dsl;

  public boolean save(EnrollmentDbO enrollment) {
    enrollment.record.attach(dsl.configuration());
    return enrollment.record.store() > 0;
  }

  public List<EnrolledCourseInfo> getEnrolledCourses(UUID userId) {
    var records = dsl.select(
        Courses.COURSES.COURSE_ID,
        Courses.COURSES.TITLE,
        Courses.COURSES.LEARNING_MODEL,
        Enrollments.ENROLLMENTS.PROGRESS_PERCENT)
        .from(Enrollments.ENROLLMENTS)
        .join(Courses.COURSES)
        .on(Enrollments.ENROLLMENTS.COURSE_ID.eq(Courses.COURSES.COURSE_ID))
        .where(Enrollments.ENROLLMENTS.USER_ID.eq(userId))
        .orderBy(Enrollments.ENROLLMENTS.ENROLLED_AT.desc())
        .fetch();

    if (records == null) {
      return null;
    }

    List<EnrolledCourseInfo> list = new ArrayList<>();
    for (var record : records) {
      Double progress = record.value4();
      list.add(new EnrolledCourseInfo(
          record.value1(),
          record.value2(),
          record.value3(),
          progress != null ? progress : 0.0));
    }
    return list;
  }

  public void markCourseAsCompleted(UUID userId, UUID courseId) {
    dsl.update(Enrollments.ENROLLMENTS)
        .set(Enrollments.ENROLLMENTS.IS_COMPLETED, true)
        .set(Enrollments.ENROLLMENTS.COMPLETED_AT, java.time.LocalDateTime.now())
        .where(Enrollments.ENROLLMENTS.USER_ID.eq(userId))
        .and(Enrollments.ENROLLMENTS.COURSE_ID.eq(courseId))
        .execute();
  }

  public boolean isUserEnrolled(UUID userId, UUID courseId) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(com.eduflex.generated.tables.Enrollments.ENROLLMENTS)
            .where(com.eduflex.generated.tables.Enrollments.ENROLLMENTS.USER_ID.eq(userId))
            .and(com.eduflex.generated.tables.Enrollments.ENROLLMENTS.COURSE_ID.eq(courseId)));
  }

  public void enrollUser(UUID userId, UUID courseId) {
    var e = com.eduflex.generated.tables.Enrollments.ENROLLMENTS;
    dsl.insertInto(e)
        .set(e.USER_ID, userId)
        .set(e.COURSE_ID, courseId)
        .execute();
  }
}
