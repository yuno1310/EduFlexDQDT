package com.eduflex.service.course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.course.CreateCourseDTO.CreateCourseRequest;
import com.eduflex.dto.course.CreateCourseDTO.CreateCourseResponse;
import com.eduflex.entity.course.CourseDbO;
import com.eduflex.repository.course.CourseRepository;

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
