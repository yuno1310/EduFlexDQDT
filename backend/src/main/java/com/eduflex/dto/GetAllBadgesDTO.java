package com.eduflex.dto;

public class GetAllBadgesDTO {
    public record GetAllBadgesResponse(
        Long id,
        String name,
        String description,
        String iconUrl,
        String conditionType
    ) {}
}
