package com.eduflex.repository;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.entity.QuestionDbO;

@Repository
public class QuestionRepository {
  @Autowired
  private DSLContext dsl;

  public boolean save(QuestionDbO question) {
    question.record.attach(dsl.configuration());
    if (question.record.store() > 0) {
      return true;
    } else
      return false;
  }
}
