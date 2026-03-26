package com.eduflex.repository;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.entity.LessonDbO;

@Repository
public class LessonRepository {
  @Autowired
  private DSLContext dsl;

  public boolean save(LessonDbO lesson) {
    lesson.record.attach(dsl.configuration());
    if (lesson.record.store() > 0) {
      return true;
    } else {
      return false;
    }
  }

}
