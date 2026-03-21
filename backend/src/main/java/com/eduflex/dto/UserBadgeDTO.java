package com.eduflex.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBadgeDTO {

    private Long id;
    private String userId;
    private Long badgeId;
    private String badgeName;
    private String badgeDescription;
    private String badgeIconUrl;
    private LocalDateTime earnedAt;
}
