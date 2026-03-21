package com.eduflex.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeDTO {

    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private String conditionType;
}
