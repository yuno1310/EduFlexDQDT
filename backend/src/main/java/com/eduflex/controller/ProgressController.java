package com.eduflex.controller;

import com.eduflex.dto.ProgressDTO.SaveLessonRequest;
import com.eduflex.dto.ProgressDTO.SaveLessonResponse;
import com.eduflex.service.SaveLessonProgressUseCase;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

  @Autowired
  private SaveLessonProgressUseCase saveLessonProgressUseCase;

  @PostMapping("/lesson")
  public ResponseEntity<SaveLessonResponse> saveLessonProgress(@Valid @RequestBody SaveLessonRequest request) {
    var response = saveLessonProgressUseCase.execute(request);

    if (response.success()) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }
}
