package com.eduflex.entity;

import com.eduflex.generated.tables.Questions;
import com.eduflex.generated.tables.records.QuestionsRecord;
import java.util.UUID;

public class QuestionDbO {
  public QuestionsRecord record;

  public QuestionDbO(UUID lessonId, String questionText, Integer points) {
    record = Questions.QUESTIONS.newRecord();
    record.setLessonId(lessonId);
    record.setQuestionText(questionText);
    record.setPoints(points);
  }

  public QuestionDbO(QuestionsRecord record) {
    this.record = record;
  }
}
