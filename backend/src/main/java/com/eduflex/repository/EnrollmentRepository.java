package com.eduflex.repository;

import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.entity.EnrollmentDbO;
import com.eduflex.generated.tables.Enrollments;

@Repository
public class EnrollmentRepository {
  @Autowired
  private DSLContext dsl;

  public boolean save(EnrollmentDbO enrollment) {
    enrollment.record.attach(dsl.configuration());
    return enrollment.record.store() > 0;
  }

  public void markCourseAsCompleted(UUID userId, UUID courseId) {
    dsl.update(Enrollments.ENROLLMENTS)
        .set(Enrollments.ENROLLMENTS.IS_COMPLETED, true)
        .set(Enrollments.ENROLLMENTS.COMPLETED_AT, java.time.LocalDateTime.now())
        .where(Enrollments.ENROLLMENTS.USER_ID.eq(userId))
        .and(Enrollments.ENROLLMENTS.COURSE_ID.eq(courseId))
        .execute();
  }

}
