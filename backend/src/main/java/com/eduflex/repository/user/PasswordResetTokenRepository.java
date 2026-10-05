package com.eduflex.repository.user;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class PasswordResetTokenRepository {
  private final DSLContext dsl;

  public PasswordResetTokenRepository(DSLContext dsl) {
    this.dsl = dsl;
  }

  public void replaceForUser(UUID userId, String tokenHash, LocalDateTime expiresAt) {
    dsl.deleteFrom(org.jooq.impl.DSL.table("password_reset_tokens"))
        .where(org.jooq.impl.DSL.field("user_id", UUID.class).eq(userId))
        .execute();
    dsl.insertInto(org.jooq.impl.DSL.table("password_reset_tokens"),
            org.jooq.impl.DSL.field("token_hash"),
            org.jooq.impl.DSL.field("user_id"),
            org.jooq.impl.DSL.field("expires_at"))
        .values(tokenHash, userId, expiresAt)
        .execute();
  }

  public UUID consume(String tokenHash, LocalDateTime now) {
    return dsl.transactionResult(configuration -> {
      DSLContext tx = org.jooq.impl.DSL.using(configuration);
      var record = tx.select(org.jooq.impl.DSL.field("user_id", UUID.class))
          .from(org.jooq.impl.DSL.table("password_reset_tokens"))
          .where(org.jooq.impl.DSL.field("token_hash", String.class).eq(tokenHash))
          .and(org.jooq.impl.DSL.field("used_at", LocalDateTime.class).isNull())
          .and(org.jooq.impl.DSL.field("expires_at", LocalDateTime.class).gt(now))
          .forUpdate()
          .fetchOne();
      if (record == null) return null;
      UUID userId = record.value1();
      int updated = tx.update(org.jooq.impl.DSL.table("password_reset_tokens"))
          .set(org.jooq.impl.DSL.field("used_at", LocalDateTime.class), now)
          .where(org.jooq.impl.DSL.field("token_hash", String.class).eq(tokenHash))
          .and(org.jooq.impl.DSL.field("used_at", LocalDateTime.class).isNull())
          .execute();
      return updated == 1 ? userId : null;
    });
  }
}
