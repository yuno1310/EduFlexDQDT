package com.eduflex.repository.gamification;

import com.eduflex.dto.gamification.GetBadgeDTO.CourseBadgeResponse;
import com.eduflex.entity.gamification.BadgesDbO;
import com.eduflex.generated.tables.Badges;
import com.eduflex.generated.tables.UserBadges;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class BadgeRepository {

  @Autowired
  private DSLContext dsl;

  public List<BadgesDbO> findAll() {
    return dsl.selectFrom(Badges.BADGES)
        .fetch()
        .map(BadgesDbO::new);
  }

  public BadgesDbO save(BadgesDbO badge) {
    var result = dsl.insertInto(Badges.BADGES,
            Badges.BADGES.NAME, Badges.BADGES.DESCRIPTION, Badges.BADGES.CONDITION_TYPE)
        .values(badge.record.getName(), badge.record.getDescription(), badge.record.getConditionType())
        .onConflictDoNothing()
        .returning()
        .fetchOne();
    if (result != null) {
      return new BadgesDbO(result);
    }
    // If conflict, return existing badge
    return findByConditionType(badge.record.getConditionType());
  }

  public BadgesDbO findById(Long id) {
    var record = dsl.selectFrom(Badges.BADGES)
        .where(Badges.BADGES.ID.eq(id))
        .fetchOne();
    return record != null ? new BadgesDbO(record) : null;
  }

  public BadgesDbO findByConditionType(String conditionType) {
    var record = dsl.selectFrom(Badges.BADGES)
        .where(Badges.BADGES.CONDITION_TYPE.eq(conditionType))
        .fetchOne();
    return record != null ? new BadgesDbO(record) : null;
  }

  public CourseBadgeResponse getEarnedCourseBadge(UUID userId, UUID courseId) {
    var b = Badges.BADGES;
    var ub = UserBadges.USER_BADGES;

    var record = dsl.select(b.NAME, b.DESCRIPTION, b.ICON_URL, ub.EARNED_AT)
        .from(ub)
        .join(b).on(ub.BADGE_ID.eq(b.ID))
        .where(ub.USER_ID.eq(userId))
        .and(b.CONDITION_TYPE.eq("COURSE_" + courseId.toString()))
        .fetchOne();

    if (record != null) {
      return new CourseBadgeResponse(true, "Get Badge for user successfully",
          record.value1(),
          record.value2(),
          record.value3(),
          record.value4());
    } else {
      return new CourseBadgeResponse(false, "Failed to get badge for user when finishing course", null, null, null,
          null);
    }
  }
}
