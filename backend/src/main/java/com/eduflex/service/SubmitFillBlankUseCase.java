package com.eduflex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.eduflex.dto.FillBlankDTO.*;
import com.eduflex.repository.QuizRepository;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubmitFillBlankUseCase {

  @Autowired
  private QuizRepository quizRepository;

  public SubmitFillBlankResponse execute(SubmitFillBlankRequest request) {
    int correctCount = 0;
    List<FillBlankResultDetail> details = new ArrayList<>();

    for (BlankAnswer answer : request.answers()) {
      List<String> correctTexts = quizRepository.getCorrectTextsForQuestion(answer.questionId());
      boolean isCorrect = false;
      String matchedAnswer = "";
      if (correctTexts != null && !correctTexts.isEmpty() && answer.submittedWord() != null) {
        String sanitizedSubmit = answer.submittedWord().trim();

        for (String correct : correctTexts) {
          if (sanitizedSubmit.equalsIgnoreCase(correct.trim())) {
            isCorrect = true;
            matchedAnswer = correct;
            correctCount++;
            break;
          }
        }
      }
      String displayCorrectText = isCorrect ? matchedAnswer : (!correctTexts.isEmpty() ? correctTexts.get(0) : "");
      details.add(new FillBlankResultDetail(answer.questionId(), isCorrect, displayCorrectText));
    }

    return new SubmitFillBlankResponse(true, "Done all questions", correctCount, request.answers().size(), details);
  }
}
