package com.eduflex.service.quiz;

import com.eduflex.dto.user.AdminDTO.OptionUpdate;
import com.eduflex.dto.user.AdminDTO.UpdateQuizRequest;
import com.eduflex.dto.user.AdminDTO.UpdateQuizResponse;
import com.eduflex.repository.quiz.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateQuizUseCase {

    @Autowired
    private QuizRepository quizRepository;

    @Transactional
    public UpdateQuizResponse execute(Long questionId, UpdateQuizRequest request) {
        if (!quizRepository.questionExistsById(questionId)) {
            return new UpdateQuizResponse(false, "Question not found");
        }

        // Update question text and points
        quizRepository.updateQuestion(
                questionId,
                request.questionText(),
                request.points() != null ? request.points() : 10
        );

        // Update each option
        if (request.options() != null) {
            for (OptionUpdate opt : request.options()) {
                quizRepository.updateOption(opt.optionId(), opt.optionText(), opt.isCorrect());
            }
        }

        return new UpdateQuizResponse(true, "Quiz updated successfully");
    }
}
