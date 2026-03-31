package com.eduflex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.QuestionDTO.CreateQuestionRequest;
import com.eduflex.dto.QuestionDTO.CreateQuestionResponse;
import com.eduflex.entity.QuestionDbO;
import com.eduflex.repository.QuestionRepository;

@Service
public class CreateQuestionUseCase {
  @Autowired
  private QuestionRepository questionRepository;

  public CreateQuestionResponse execute(CreateQuestionRequest request) {
    var question = new QuestionDbO(request.lessonId(), request.questionText(), request.points());

    if (questionRepository.save(question)) {
      return new CreateQuestionResponse(true, "Create question successfully!");
    } else {
      return new CreateQuestionResponse(false, "Failed to create question.");
    }
  }
}
