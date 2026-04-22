package com.eduflex.dto;

import java.util.UUID;

public class CourseSearchDTO {
    public record CourseSuggestionResponse(
        UUID courseId,
        String title,
        String imageUrl
    ) {}
}
