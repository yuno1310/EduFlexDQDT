package com.eduflex.service;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.eduflex.dto.ReviewDTO.*;
import com.eduflex.generated.tables.CourseReviews;

import java.util.UUID;

@Service
public class SubmitReviewUseCase {

  @Autowired
  private DSLContext dsl;

  public SubmitReviewResponse execute(UUID courseId, SubmitReviewRequest request) {
    if (request.userId() == null) {
      return new SubmitReviewResponse(false, "Không tìm thấy thông tin User!");
    }
    if (request.rating() == null) {
      return new SubmitReviewResponse(false, "Vui lòng chọn số sao đánh giá!");
    }
    if (request.comment() == null || request.comment().trim().isEmpty()) {
      return new SubmitReviewResponse(false, "Vui lòng nhập nội dung đánh giá!");
    }
    if (request.rating() < 1 || request.rating() > 5) {
      return new SubmitReviewResponse(false, "Rating must be between 1 and 5 stars!");
    }

    var r = CourseReviews.COURSE_REVIEWS;

    dsl.insertInto(r)
        .set(r.USER_ID, request.userId())
        .set(r.COURSE_ID, courseId)
        .set(r.RATING, request.rating())
        .set(r.COMMENT, request.comment())
        .onConflict(r.USER_ID, r.COURSE_ID)
        .doUpdate()
        .set(r.RATING, request.rating())
        .set(r.COMMENT, request.comment())
        .execute();

    return new SubmitReviewResponse(true, "Review saved successfully!");
  }
}
