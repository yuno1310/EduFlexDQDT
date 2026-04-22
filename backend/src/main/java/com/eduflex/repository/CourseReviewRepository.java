package com.eduflex.repository;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.generated.tables.CourseReviews;

import java.util.UUID;

@Repository
public class CourseReviewRepository {

  @Autowired
  private DSLContext dsl;

  public void upsertReview(UUID userId, UUID courseId, Integer rating, String comment) {
    var r = CourseReviews.COURSE_REVIEWS;

    dsl.insertInto(r)
        .set(r.USER_ID, userId)
        .set(r.COURSE_ID, courseId)
        .set(r.RATING, rating)
        .set(r.COMMENT, comment)
        .onConflict(r.USER_ID, r.COURSE_ID)
        .doUpdate()
        .set(r.RATING, rating)
        .set(r.COMMENT, comment)
        .execute();
  }
}
