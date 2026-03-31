package com.eduflex.entity;

import com.eduflex.generated.tables.QuestionOptions;
import com.eduflex.generated.tables.records.QuestionOptionsRecord;

public class QuestionOptionDbO {
  public QuestionOptionsRecord record;

  public QuestionOptionDbO(Long questionId, String optionText, Boolean isCorrect) {
    record = QuestionOptions.QUESTION_OPTIONS.newRecord();
    record.setQuestionId(questionId);
    record.setOptionText(optionText);
    record.setIsCorrect(isCorrect);
  }

  public QuestionOptionDbO(QuestionOptionsRecord record) {
    this.record = record;
  }
}
