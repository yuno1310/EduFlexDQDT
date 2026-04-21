package com.eduflex.android.model;

import java.util.List;

public class SubmitFillBlankRequest {
    private String userId;
    private String lessonId;
    private List<BlankAnswer> answers;

    public SubmitFillBlankRequest(String userId, String lessonId, List<BlankAnswer> answers) {
        this.userId = userId;
        this.lessonId = lessonId;
        this.answers = answers;
    }

    public static class BlankAnswer {
        private long questionId;
        private String submittedWord;

        public BlankAnswer(long questionId, String submittedWord) {
            this.questionId = questionId;
            this.submittedWord = submittedWord;
        }
    }
}
