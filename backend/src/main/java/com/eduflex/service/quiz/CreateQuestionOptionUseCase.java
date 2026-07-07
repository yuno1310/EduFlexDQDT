package com.eduflex.service.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.quiz.QuestionDTO.CreateOptionRequest;
import com.eduflex.dto.quiz.QuestionDTO.CreateOptionResponse;
import com.eduflex.entity.quiz.QuestionOptionDbO;
import com.eduflex.repository.quiz.QuestionOptionRepository;

@Service
public class CreateQuestionOptionUseCase {
  @Autowired
  private QuestionOptionRepository optionRepository;

  public CreateOptionResponse execute(CreateOptionRequest request) {
    var option = new QuestionOptionDbO(request.questionId(), request.optionText(), request.isCorrect());
    if (optionRepository.save(option)) {
      return new CreateOptionResponse(true, "Add option successfully!");
    } else {
      return new CreateOptionResponse(false, "Failed to add option.");
    }
  }
}
