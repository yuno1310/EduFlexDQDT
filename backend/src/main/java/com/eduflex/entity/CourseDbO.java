package com.eduflex.entity;

import com.eduflex.generated.tables.Courses;
import com.eduflex.generated.tables.records.CoursesRecord;

public class CourseDbO {
  public CoursesRecord record;

  public CourseDbO(String title, String learning_mode, String status) {
    record = Courses.COURSES.newRecord();
    record.setTitle(title);
    record.setLearningModel(learning_mode);
    record.setStatus(status);
  }

  public CourseDbO(CoursesRecord record) {
    this.record = record;
  }
}
