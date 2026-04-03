package com.eduflex.android.model;

import java.util.List;

public class SubmitQuizRequest {
    private String userId;
    private String lessonId;
    private List<AnswerItem> answers;

    public SubmitQuizRequest(String userId, String lessonId, List<AnswerItem> answers) {
        this.userId = userId;
        this.lessonId = lessonId;
        this.answers = answers;
    }

    public static class AnswerItem {
        private long questionId;
        private long selectedOptionId;

        public AnswerItem(long questionId, long selectedOptionId) {
            this.questionId = questionId;
            this.selectedOptionId = selectedOptionId;
        }
    }
}
