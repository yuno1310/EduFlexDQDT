package com.eduflex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduflex.dto.AdminDTO.DeleteQuestionResponse;
import com.eduflex.repository.QuizRepository;

@Service
public class DeleteQuestionUseCase {

    @Autowired
    private QuizRepository quizRepository;

    @Transactional
    public DeleteQuestionResponse execute(Long questionId) {
        if (!quizRepository.questionExistsById(questionId)) {
            return new DeleteQuestionResponse(false, "Question not found");
        }
        try {
            quizRepository.deleteOptionsByQuestionId(questionId);
            quizRepository.deleteQuestion(questionId);
            return new DeleteQuestionResponse(true, "Question deleted successfully");
        } catch (Exception e) {
            return new DeleteQuestionResponse(false, "Failed to delete question: " + e.getMessage());
        }
    }
}
