package com.eduflex.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AdminDTO {

    public record AdminUserItem(
            UUID userId,
            String email,
            String fullName,
            String avatarUrl,
            LocalDateTime createdAt,
            int xp,
            int level,
            int streakDays,
            String lastStudyDate,
            String role
    ) {}

    public record GetAllUsersResponse(
            boolean success,
            String message,
            List<AdminUserItem> users
    ) {}

    public record DeleteUserResponse(
            boolean success,
            String message
    ) {}

    public record DeleteCourseResponse(
            boolean success,
            String message
    ) {}

    // === Update Course ===
    public record UpdateCourseRequest(
            String title,
            String learningModel,
            String status,
            String imageUrl,
            Long price,
            String description
    ) {}

    public record UpdateCourseResponse(
            boolean success,
            String message
    ) {}

    // === Update Lesson ===
    public record UpdateLessonRequest(
            String title,
            String contentType,
            String videoUrl,
            String content
    ) {}

    public record UpdateLessonResponse(
            boolean success,
            String message
    ) {}

    public record DeleteLessonResponse(
            boolean success,
            String message
    ) {}

    // === Update Quiz ===
    public record OptionUpdate(
            Long optionId,
            String optionText,
            Boolean isCorrect
    ) {}

    public record UpdateQuizRequest(
            String questionText,
            Integer points,
            java.util.List<OptionUpdate> options
    ) {}

    public record UpdateQuizResponse(
            boolean success,
            String message
    ) {}
}
