package com.eduflex.repository;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.entity.CourseDbO;

@Repository
public class CourseRepository {
  @Autowired
  private DSLContext dsl;

  public boolean save(CourseDbO course) {
    course.record.attach(dsl.configuration());
    if (course.record.store() > 0) {
      return true;
    } else {
      return false;
    }
  }
}
