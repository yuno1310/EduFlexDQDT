package com.eduflex.entity;

import com.eduflex.generated.tables.UserBadges;
import com.eduflex.generated.tables.records.UserBadgesRecord;
import java.util.UUID;

public class UserBadgesDbO {
    public UserBadgesRecord record;

    public UserBadgesDbO(UUID userId, Long badgeId) {
        record = UserBadges.USER_BADGES.newRecord();
        record.setUserId(userId);
        record.setBadgeId(badgeId);
    }

    public UserBadgesDbO(UserBadgesRecord record) {
        this.record = record;
    }
}
