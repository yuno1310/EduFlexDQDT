package com.eduflex.repository;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.entity.EnrollmentDbO;

@Repository
public class EnrollmentRepository {
  @Autowired
  private DSLContext dsl;

  public boolean save(EnrollmentDbO enrollment) {
    enrollment.record.attach(dsl.configuration());
    return enrollment.record.store() > 0;
  }
}
