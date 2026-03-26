package com.eduflex.repository;

import java.util.ArrayList;
import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.dto.GetCourseDTO.CourseInfo;
import com.eduflex.entity.CourseDbO;
import com.eduflex.generated.tables.Courses;

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

  public List<CourseInfo> get_course() {
    var records = dsl.select(
        Courses.COURSES.TITLE,
        Courses.COURSES.LEARNING_MODEL,
        Courses.COURSES.STATUS)
        .from(Courses.COURSES)
        .fetch();
    if (records != null) {
      List<CourseInfo> list = new ArrayList<CourseInfo>();
      for (var record : records) {
        CourseInfo course = new CourseInfo(record.value1(), record.value2(), record.value3());
        list.add(course);
      }
      return list;
    } else {
      return null;
    }
  }
}
