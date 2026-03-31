package com.eduflex.repository;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.entity.LessonProgressDbO;

@Repository
public class LessonProgressBaseRepository {
  @Autowired
  private DSLContext dsl;

  public boolean save(LessonProgressDbO progress) {
    progress.record.attach(dsl.configuration());
    return progress.record.store() > 0;
  }
}
