package com.eduflex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduflex.dto.QuizDTO.CreateQuizRequest;
import com.eduflex.dto.QuizDTO.CreateQuizResponse;
import com.eduflex.entity.QuestionDbO;
import com.eduflex.entity.QuestionOptionDbO;
import com.eduflex.repository.QuizRepository;

@Service
public class CreateQuizUseCase {
  @Autowired
  private QuizRepository quizRepository;

  @Transactional
  public CreateQuizResponse execute(CreateQuizRequest request) {
    try {
      var question = new QuestionDbO(request.lessonId(), request.questionText(), request.points());
      Long questionId = quizRepository.saveQuestionAndGetId(question);

      for (var opt : request.options()) {
        var optionDbO = new QuestionOptionDbO(questionId, opt.optionText(), opt.isCorrect());
        quizRepository.saveOption(optionDbO);
      }

      return new CreateQuizResponse(true, "Create Quiz with options successfully!");
    } catch (Exception e) {
      e.printStackTrace();
      return new CreateQuizResponse(false, "Failed to create Quiz: " + e.getMessage());
    }
  }
}
