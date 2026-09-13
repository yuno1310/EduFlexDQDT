package com.eduflex.service.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.quiz.QuizDTO.GetQuizResponse;
import com.eduflex.dto.quiz.QuizDTO.OptionResponse;
import com.eduflex.dto.quiz.QuizDTO.QuestionResponse;
import com.eduflex.generated.tables.QuestionOptions;
import com.eduflex.generated.tables.Questions;
import com.eduflex.repository.quiz.QuizRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetQuizUseCase {
  @Autowired
  private QuizRepository quizRepository;

  public GetQuizResponse execute(UUID lessonId) {
    var questionRecords = quizRepository.getQuestionsByLessonId(lessonId);
    if (questionRecords == null || questionRecords.isEmpty()) {
      return new GetQuizResponse(false, "Quiz not found for this lesson", null, null);
    }

    UUID parentLessonId = quizRepository.getParentLessonId(lessonId);

    List<QuestionResponse> questions = new ArrayList<>();
    for (var q : questionRecords) {
      Long qId = q.get(Questions.QUESTIONS.QUESTION_ID);
      String qText = q.get(Questions.QUESTIONS.QUESTION_TEXT);
      int points = q.get(Questions.QUESTIONS.POINTS);

      List<OptionResponse> options = quizRepository.getOptionsByQuestionId(qId).stream()
          .map(opt -> new OptionResponse(
              opt.get(QuestionOptions.QUESTION_OPTIONS.OPTION_ID),
              opt.get(QuestionOptions.QUESTION_OPTIONS.OPTION_TEXT)))
          .collect(Collectors.toList());

      questions.add(new QuestionResponse(qId, qText, points, options));
    }

    return new GetQuizResponse(true, "Success", questions, parentLessonId);
  }
}
