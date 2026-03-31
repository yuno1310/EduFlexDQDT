package com.eduflex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.QuestionDTO.CreateOptionRequest;
import com.eduflex.dto.QuestionDTO.CreateOptionResponse;
import com.eduflex.entity.QuestionOptionDbO;
import com.eduflex.repository.QuestionOptionRepository;

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
