package com.eduflex.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.GetCourseDTO.CourseInfo;
import com.eduflex.dto.GetCourseDTO.GetCourseResponse;
import com.eduflex.repository.UserRepository;
import com.eduflex.repository.CourseRepository;
import java.util.UUID;

@Service
public class GetCourseUseCase {
  @Autowired
  private CourseRepository courseRepository;
  
  @Autowired
  private UserRepository userRepository;

  public GetCourseResponse execute(UUID userId) {
    boolean isAdmin = false;
    if (userId != null) {
      var user = userRepository.find_by_id(userId);
      if (user != null && "admin".equalsIgnoreCase(user.record.getRole())) {
        isAdmin = true;
      }
    }

    var courses = isAdmin ? courseRepository.get_course() : courseRepository.get_active_course();

    if (courses != null) {
      var listCourses = new ArrayList<CourseInfo>();
      for (var record : courses) {
        var course = new CourseInfo(record.courseID(), record.title(), record.learningMode(), record.status(),
            record.description(), record.imageUrl(), record.price());
        listCourses.add(course);
      }
      return new GetCourseResponse(true, "Loading list courses successfuly", listCourses);
    } else {
      return new GetCourseResponse(false, "Failed to load list courses", null);
    }
  }
}
