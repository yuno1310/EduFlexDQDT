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

  public CourseDbO(String title, String learning_mode, String status,
                   String description, String imageUrl, Long price) {
    record = Courses.COURSES.newRecord();
    record.setTitle(title);
    record.setLearningModel(learning_mode);
    record.setStatus(status);
    if (description != null) record.setDescription(description);
    if (imageUrl != null) record.setImageUrl(imageUrl);
    if (price != null) record.setPrice(price);
  }

  public CourseDbO(CoursesRecord record) {
    this.record = record;
  }
}
