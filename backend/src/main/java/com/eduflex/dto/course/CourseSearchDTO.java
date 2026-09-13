package com.eduflex.dto.course;

import java.util.UUID;

public class CourseSearchDTO {
    public record CourseSuggestionResponse(
        UUID courseId,
        String title,
        String imageUrl
    ) {}
}
