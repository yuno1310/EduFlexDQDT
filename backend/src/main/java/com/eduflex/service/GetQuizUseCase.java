package com.eduflex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.QuizDTO.GetQuizResponse;
import com.eduflex.dto.QuizDTO.OptionResponse;
import com.eduflex.generated.tables.Questions;
import com.eduflex.generated.tables.QuestionOptions;
import com.eduflex.repository.QuizRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetQuizUseCase {
  @Autowired
  private QuizRepository quizRepository;

  public GetQuizResponse execute(UUID lessonId) {
    var questionRecord = quizRepository.getQuestionByLessonId(lessonId);
    if (questionRecord == null) {
      return new GetQuizResponse(false, "Quiz not found for this lesson", null, null, 0, null);
    }

    Long qId = questionRecord.get(Questions.QUESTIONS.QUESTION_ID);
    String qText = questionRecord.get(Questions.QUESTIONS.QUESTION_TEXT);
    int points = questionRecord.get(Questions.QUESTIONS.POINTS);

    var optionRecords = quizRepository.getOptionsByQuestionId(qId);
    List<OptionResponse> options = optionRecords.stream()
        .map(opt -> new OptionResponse(
            opt.get(QuestionOptions.QUESTION_OPTIONS.OPTION_ID),
            opt.get(QuestionOptions.QUESTION_OPTIONS.OPTION_TEXT)))
        .collect(Collectors.toList());

    return new GetQuizResponse(true, "Success", qId, qText, points, options);
  }
}
