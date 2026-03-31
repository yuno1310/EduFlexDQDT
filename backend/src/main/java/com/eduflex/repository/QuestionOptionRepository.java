package com.eduflex.repository;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.eduflex.entity.QuestionOptionDbO;

@Repository
public class QuestionOptionRepository {
  private DSLContext dsl;

  public boolean save(QuestionOptionDbO option) {
    option.record.attach(dsl.configuration());
    return option.record.store() > 0;
  }
}
