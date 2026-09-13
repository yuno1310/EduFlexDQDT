package com.eduflex.repository.quiz;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.eduflex.entity.quiz.QuestionOptionDbO;

@Repository
public class QuestionOptionRepository {
  private DSLContext dsl;

  public boolean save(QuestionOptionDbO option) {
    option.record.attach(dsl.configuration());
    return option.record.store() > 0;
  }
}
