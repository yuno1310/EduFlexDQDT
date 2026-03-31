package com.eduflex.repository;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.entity.QuizAttemptDbO;

@Repository
public class QuizAttempRepository {
  @Autowired
  private DSLContext dsl;

  public boolean save(QuizAttemptDbO attemp) {
    attemp.record.attach(dsl.configuration());
    return attemp.record.store() > 0;
  }
}
