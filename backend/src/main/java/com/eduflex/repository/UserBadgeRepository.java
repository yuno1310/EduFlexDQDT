package com.eduflex.repository;

import com.eduflex.entity.UserBadgesDbO;
import com.eduflex.generated.tables.UserBadges;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class UserBadgeRepository {

    @Autowired
    private DSLContext dsl;

    public List<UserBadgesDbO> findByUserId(UUID userId) {
        return dsl.selectFrom(UserBadges.USER_BADGES)
                .where(UserBadges.USER_BADGES.USER_ID.eq(userId))
                .fetch()
                .map(UserBadgesDbO::new);
    }

    public boolean existsByUserIdAndBadgeId(UUID userId, Long badgeId) {
        return dsl.fetchCount(
                dsl.selectFrom(UserBadges.USER_BADGES)
                        .where(UserBadges.USER_BADGES.USER_ID.eq(userId))
                        .and(UserBadges.USER_BADGES.BADGE_ID.eq(badgeId))
        ) > 0;
    }

    public boolean save(UserBadgesDbO userBadge) {
        userBadge.record.attach(dsl.configuration());
        return userBadge.record.store() > 0;
    }
}
