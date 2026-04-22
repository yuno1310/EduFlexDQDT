package com.eduflex.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CourseReviewRepository {

  @Autowired
  private DSLContext dsl;

  private static final Table<?> COURSE_REVIEWS = DSL.table(DSL.name("course_reviews"));
  private static final Table<?> USERS = DSL.table(DSL.name("users"));

  private static final Field<UUID> REVIEW_USER_ID = DSL.field(DSL.name("course_reviews", "user_id"), UUID.class);
  private static final Field<UUID> REVIEW_COURSE_ID = DSL.field(DSL.name("course_reviews", "course_id"), UUID.class);
  private static final Field<Integer> REVIEW_RATING = DSL.field(DSL.name("course_reviews", "rating"), Integer.class);
  private static final Field<String> REVIEW_COMMENT = DSL.field(DSL.name("course_reviews", "comment"), String.class);
  private static final Field<LocalDateTime> REVIEW_CREATED_AT = DSL.field(DSL.name("course_reviews", "created_at"),
      LocalDateTime.class);

  private static final Field<UUID> USER_ID = DSL.field(DSL.name("users", "user_id"), UUID.class);
  private static final Field<String> USER_FULL_NAME = DSL.field(DSL.name("users", "full_name"), String.class);

  public record CourseReviewRow(
      UUID userId,
      String reviewerName,
      int rating,
      String comment,
      LocalDateTime createdAt) {
  }

  public boolean upsertReview(UUID userId, UUID courseId, int rating, String comment) {
    int affected = dsl.insertInto(COURSE_REVIEWS)
        .columns(REVIEW_USER_ID, REVIEW_COURSE_ID, REVIEW_RATING, REVIEW_COMMENT, REVIEW_CREATED_AT)
        .values(userId, courseId, rating, comment, LocalDateTime.now())
        .onConflict(REVIEW_USER_ID, REVIEW_COURSE_ID)
        .doUpdate()
        .set(REVIEW_RATING, rating)
        .set(REVIEW_COMMENT, comment)
        .set(REVIEW_CREATED_AT, LocalDateTime.now())
        .execute();
    return affected > 0;
  }

  public List<CourseReviewRow> getCourseReviews(UUID courseId) {
    var records = dsl.select(REVIEW_USER_ID, USER_FULL_NAME, REVIEW_RATING, REVIEW_COMMENT, REVIEW_CREATED_AT)
        .from(COURSE_REVIEWS)
        .leftJoin(USERS)
        .on(REVIEW_USER_ID.eq(USER_ID))
        .where(REVIEW_COURSE_ID.eq(courseId))
        .orderBy(REVIEW_CREATED_AT.desc())
        .fetch();

    List<CourseReviewRow> result = new ArrayList<>();
    for (var record : records) {
      Integer rating = record.get(REVIEW_RATING);
      if (rating == null) {
        continue;
      }
      result.add(new CourseReviewRow(
          record.get(REVIEW_USER_ID),
          record.get(USER_FULL_NAME),
          rating,
          record.get(REVIEW_COMMENT),
          record.get(REVIEW_CREATED_AT)));
    }
    return result;
  }
}
