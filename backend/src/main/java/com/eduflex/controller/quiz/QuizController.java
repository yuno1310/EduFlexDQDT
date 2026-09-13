package com.eduflex.controller.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.eduflex.dto.quiz.FillBlankDTO.SubmitFillBlankRequest;
import com.eduflex.dto.quiz.FillBlankDTO.SubmitFillBlankResponse;
import com.eduflex.dto.quiz.QuizDTO.CreateQuizRequest;
import com.eduflex.dto.quiz.QuizDTO.CreateQuizResponse;
import com.eduflex.dto.quiz.QuizDTO.GetQuizResponse;
import com.eduflex.dto.quiz.QuizDTO.SubmitQuizRequest;
import com.eduflex.dto.quiz.QuizDTO.SubmitQuizResponse;
import com.eduflex.service.quiz.CreateQuizUseCase;
import com.eduflex.service.quiz.GetQuizUseCase;
import com.eduflex.service.quiz.SubmitFillBlankUseCase;
import com.eduflex.service.quiz.SubmitQuizUseCase;

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
  @Autowired
  private SubmitFillBlankUseCase submitFillBlankUseCase;

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

  @PostMapping("/submit-multiple-choice")
  public ResponseEntity<SubmitQuizResponse> submitQuiz(@RequestBody SubmitQuizRequest request) {
    var response = submitQuizUseCase.execute(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/fill-blank")
  public ResponseEntity<SubmitFillBlankResponse> submitFillBlank(
      @RequestBody SubmitFillBlankRequest request) {
    var response = submitFillBlankUseCase.execute(request);
    if (response.success() == true) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }
}
