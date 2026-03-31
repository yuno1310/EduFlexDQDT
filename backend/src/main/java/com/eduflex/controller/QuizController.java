package com.eduflex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.eduflex.dto.QuizDTO.CreateQuizRequest;
import com.eduflex.dto.QuizDTO.CreateQuizResponse;
import com.eduflex.dto.QuizDTO.GetQuizResponse;
import com.eduflex.dto.QuizDTO.SubmitQuizRequest;
import com.eduflex.dto.QuizDTO.SubmitQuizResponse;
import com.eduflex.service.CreateQuizUseCase;
import com.eduflex.service.GetQuizUseCase;
import com.eduflex.service.SubmitQuizUseCase;

import java.util.UUID;

@RestController
@RequestMapping("api/quiz")
public class QuizController {

  @Autowired
  private CreateQuizUseCase createQuizUseCase;
  @Autowired
  private GetQuizUseCase getQuizUseCase;
  @Autowired
  private SubmitQuizUseCase submitQuizUseCase;

  @PostMapping("/create")
  public ResponseEntity<CreateQuizResponse> createQuiz(@RequestBody CreateQuizRequest request) {
    var response = createQuizUseCase.execute(request);
    return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
  }

  @GetMapping("/{lessonId}")
  public ResponseEntity<GetQuizResponse> getQuiz(@PathVariable UUID lessonId) {
    var response = getQuizUseCase.execute(lessonId);
    return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
  }

  @PostMapping("/submit")
  public ResponseEntity<SubmitQuizResponse> submitQuiz(@RequestBody SubmitQuizRequest request) {
    var response = submitQuizUseCase.execute(request);
    return ResponseEntity.ok(response);
  }
}
