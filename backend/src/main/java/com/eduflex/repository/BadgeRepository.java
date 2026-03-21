package com.eduflex.repository;

import com.eduflex.entity.BadgesDbO;
import com.eduflex.generated.tables.Badges;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BadgeRepository {

    @Autowired
    private DSLContext dsl;

    public List<BadgesDbO> findAll() {
        return dsl.selectFrom(Badges.BADGES)
                .fetch()
                .map(BadgesDbO::new);
    }

    public BadgesDbO findById(Long id) {
        var record = dsl.selectFrom(Badges.BADGES)
                .where(Badges.BADGES.ID.eq(id))
                .fetchOne();
        return record != null ? new BadgesDbO(record) : null;
    }
}
