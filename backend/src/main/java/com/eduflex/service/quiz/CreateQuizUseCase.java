package com.eduflex.service.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduflex.dto.quiz.QuizDTO.CreateQuizRequest;
import com.eduflex.dto.quiz.QuizDTO.CreateQuizResponse;
import com.eduflex.entity.quiz.QuestionDbO;
import com.eduflex.entity.quiz.QuestionOptionDbO;
import com.eduflex.repository.quiz.QuizRepository;

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
