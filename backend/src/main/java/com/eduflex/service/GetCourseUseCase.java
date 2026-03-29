package com.eduflex.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.GetCourseDTO.CourseInfo;
import com.eduflex.dto.GetCourseDTO.GetCourseResponse;
import com.eduflex.repository.CourseRepository;

@Service
public class GetCourseUseCase {
  @Autowired
  private CourseRepository courseRepository;

  public GetCourseResponse execute() {
    var courses = courseRepository.get_course();
    if (courses != null) {
      var listCourses = new ArrayList<CourseInfo>();
      for (var record : courses) {
        var course = new CourseInfo(record.courseID(), record.title(), record.learningMode(), record.status());
        listCourses.add(course);
      }
      return new GetCourseResponse(true, "Loading list courses successfuly", listCourses);
    } else {
      return new GetCourseResponse(false, "Failed to load list courses", null);
    }
  }
}
