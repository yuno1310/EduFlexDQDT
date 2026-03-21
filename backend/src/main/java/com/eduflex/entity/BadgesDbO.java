package com.eduflex.entity;

import com.eduflex.generated.tables.Badges;
import com.eduflex.generated.tables.records.BadgesRecord;

public class BadgesDbO {
    public BadgesRecord record;

    public BadgesDbO(String name, String description, String iconUrl, String conditionType) {
        record = Badges.BADGES.newRecord();
        record.setName(name);
        record.setDescription(description);
        record.setIconUrl(iconUrl);
        record.setConditionType(conditionType);
    }

    public BadgesDbO(BadgesRecord record) {
        this.record = record;
    }
}
