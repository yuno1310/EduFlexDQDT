package com.eduflex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduflex.dto.CreateCourseDTO.CreateCourseRequest;
import com.eduflex.dto.CreateCourseDTO.CreateCourseResponse;
import com.eduflex.service.CreateCourseUseCase;

@RestController
@RequestMapping("api/course")
public class CourseController {
  @Autowired
  private CreateCourseUseCase createCourseUseCase;

  @PostMapping
  public ResponseEntity<CreateCourseResponse> createCourse(CreateCourseRequest request) {
    var response = createCourseUseCase.execute(request);
    if (response.sucess() == true) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }
}
