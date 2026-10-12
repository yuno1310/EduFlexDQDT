package com.eduflex.service.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.eduflex.dto.quiz.FillBlankDTO.*;
import com.eduflex.repository.quiz.QuizRepository;
import com.eduflex.security.CourseAccess;
import com.eduflex.repository.lesson.LessonProgressRepository;
import com.eduflex.exception.ResourceNotFoundException;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubmitFillBlankUseCase {

  @Autowired
  private QuizRepository quizRepository;

  @Autowired
  private CourseAccess courseAccess;
  @Autowired
  private LessonProgressRepository lessons;

  public SubmitFillBlankResponse execute(SubmitFillBlankRequest request) {
    var courseId = lessons.getCourseIdByLessonId(request.lessonId());
    if (courseId == null) {
      throw new ResourceNotFoundException("Quiz lesson not found");
    }
    courseAccess.requireEnrollment(request.userId(), courseId);
    if (!lessons.isQuizLesson(request.lessonId())) {
      throw new IllegalArgumentException("The selected lesson is not a quiz");
    }
    var questionIds = quizRepository.getQuestionsByLessonId(request.lessonId()).stream()
        .map(q -> q.getQuestionId()).collect(Collectors.toSet());
    var submittedIds = new HashSet<Long>();
    for (BlankAnswer answer : request.answers()) {
      if (answer == null || !questionIds.contains(answer.questionId())
          || !submittedIds.add(answer.questionId())) {
        throw new IllegalArgumentException("Each answer must identify a unique question in this quiz");
      }
    }
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
