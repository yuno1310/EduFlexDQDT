package com.eduflex.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GamificationStatsDTO {

    private Long id;
    private String userId;
    private int xp;
    private int level;
    private int streakDays;
    private LocalDate lastStudyDate;
}
