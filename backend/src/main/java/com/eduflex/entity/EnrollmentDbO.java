package com.eduflex.entity;

import com.eduflex.generated.tables.Enrollments;
import com.eduflex.generated.tables.records.EnrollmentsRecord;
import java.util.UUID;
import java.time.LocalDateTime;

public class EnrollmentDbO {
  public EnrollmentsRecord record;

  public EnrollmentDbO(UUID userId, UUID courseId) {
    record = Enrollments.ENROLLMENTS.newRecord();
    record.setUserId(userId);
    record.setCourseId(courseId);
    record.setProgressPercent(0.0);
    record.setAiDropoutRiskScore(0.0);
    record.setEnrolledAt(LocalDateTime.now());
  }

  public EnrollmentDbO(EnrollmentsRecord record) {
    this.record = record;
  }
}
