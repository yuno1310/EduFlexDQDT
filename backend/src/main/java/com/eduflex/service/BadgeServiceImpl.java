package com.eduflex.service;

import com.eduflex.dto.BadgeDTO;
import com.eduflex.dto.UserBadgeDTO;
import com.eduflex.exception.ResourceNotFoundException;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

@Service
public class BadgeServiceImpl implements BadgeService {

    private final DSLContext dsl;

    public BadgeServiceImpl(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<BadgeDTO> getAllBadges() {
        return dsl.selectFrom(table("badges"))
                .fetch()
                .stream()
                .map(this::mapToBadgeDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserBadgeDTO> getUserBadges(String userId) {
        return dsl.select()
                .from(table("user_badges"))
                .join(table("badges")).on(field("user_badges.badge_id").eq(field("badges.id")))
                .where(field("user_badges.user_id").eq(userId))
                .fetch()
                .stream()
                .map(this::mapToUserBadgeDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserBadgeDTO awardBadge(String userId, Long badgeId) {
        // Verify user exists
        var userExists = dsl.selectCount()
                .from(table("users"))
                .where(field("user_id").eq(userId))
                .fetchOne(0, int.class);

        if (userExists == null || userExists == 0) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Verify badge exists
        Record badgeRecord = dsl.selectFrom(table("badges"))
                .where(field("id").eq(badgeId))
                .fetchOne();

        if (badgeRecord == null) {
            throw new ResourceNotFoundException("Badge not found with id: " + badgeId);
        }

        // Check if already awarded
        var alreadyAwarded = dsl.selectCount()
                .from(table("user_badges"))
                .where(field("user_id").eq(userId))
                .and(field("badge_id").eq(badgeId))
                .fetchOne(0, int.class);

        if (alreadyAwarded != null && alreadyAwarded > 0) {
            throw new IllegalStateException("Badge already awarded to this user");
        }

        // Award the badge
        dsl.insertInto(table("user_badges"))
                .set(field("user_id"), userId)
                .set(field("badge_id"), badgeId)
                .execute();

        return UserBadgeDTO.builder()
                .userId(userId)
                .badgeId(badgeId)
                .badgeName(badgeRecord.get(field("name", String.class)))
                .badgeDescription(badgeRecord.get(field("description", String.class)))
                .badgeIconUrl(badgeRecord.get(field("icon_url", String.class)))
                .earnedAt(LocalDateTime.now())
                .build();
    }

    // ── Mapping helpers ──

    private BadgeDTO mapToBadgeDTO(Record record) {
        return BadgeDTO.builder()
                .id(record.get(field("id", Long.class)))
                .name(record.get(field("name", String.class)))
                .description(record.get(field("description", String.class)))
                .iconUrl(record.get(field("icon_url", String.class)))
                .conditionType(record.get(field("condition_type", String.class)))
                .build();
    }

    private UserBadgeDTO mapToUserBadgeDTO(Record record) {
        return UserBadgeDTO.builder()
                .id(record.get(field("user_badges.id", Long.class)))
                .userId(record.get(field("user_badges.user_id", String.class)))
                .badgeId(record.get(field("user_badges.badge_id", Long.class)))
                .badgeName(record.get(field("badges.name", String.class)))
                .badgeDescription(record.get(field("badges.description", String.class)))
                .badgeIconUrl(record.get(field("badges.icon_url", String.class)))
                .earnedAt(record.get(field("user_badges.earned_at", LocalDateTime.class)))
                .build();
    }
}
