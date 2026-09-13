package com.eduflex.repository.quiz;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.entity.quiz.QuizAttemptDbO;

@Repository
public class QuizAttempRepository {
  @Autowired
  private DSLContext dsl;

  public boolean save(QuizAttemptDbO attemp) {
    attemp.record.attach(dsl.configuration());
    return attemp.record.store() > 0;
  }
}
