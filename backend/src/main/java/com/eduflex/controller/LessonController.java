package com.eduflex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduflex.dto.CreateLessonDTO.CreateLessonRequest;
import com.eduflex.dto.CreateLessonDTO.CreateLessonResponse;
import com.eduflex.dto.GetLessonDTO.GetLessonRequest;
import com.eduflex.dto.GetLessonDTO.GetLessonResponse;
import com.eduflex.service.CreateLessonUseCase;
import com.eduflex.service.GetLessonUseCase;

@RestController
@RequestMapping("api/lesson")
public class LessonController {
  @Autowired
  private CreateLessonUseCase createLessonUseCase;

  @Autowired
  private GetLessonUseCase getLessonUseCase;

  @PostMapping
  public ResponseEntity<CreateLessonResponse> createLesson(CreateLessonRequest request) {
    var response = createLessonUseCase.execute(request);
    if (response.success() == true) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }

  @GetMapping
  public ResponseEntity<GetLessonResponse> getListLesson(GetLessonRequest request) {
    var response = getLessonUseCase.execute(request);
    if (response.success() == true) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }
}
