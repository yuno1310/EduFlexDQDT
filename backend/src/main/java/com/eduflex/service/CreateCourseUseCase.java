package com.eduflex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.CreateCourseDTO.CreateCourseRequest;
import com.eduflex.dto.CreateCourseDTO.CreateCourseResponse;
import com.eduflex.entity.CourseDbO;
import com.eduflex.repository.CourseRepository;

@Service
public class CreateCourseUseCase {
  @Autowired
  private CourseRepository courseRepository;

  public CreateCourseResponse execute(CreateCourseRequest request) {
    var course = new CourseDbO(
        request.title(),
        request.learningModel(),
        request.status(),
        request.description(),
        request.imageUrl(),
        request.price()
    );
    if (courseRepository.save(course) == true) {
      return new CreateCourseResponse(true, "Create a new course sucessfully");
    } else {
      return new CreateCourseResponse(false, "Failed to create new course");
    }
  }
}
