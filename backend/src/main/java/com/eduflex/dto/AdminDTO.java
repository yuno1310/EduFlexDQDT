package com.eduflex.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AdminDTO {

    public record AdminUserItem(
            UUID userId,
            String email,
            String fullName,
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
}
