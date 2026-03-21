package com.eduflex.entity;

import com.eduflex.generated.tables.Users;
import com.eduflex.generated.tables.records.UsersRecord;

public class UsersDbO {
  public UsersRecord record;

  public UsersDbO(String email, String password_hash, String fullName,
      boolean active) {
    record = Users.USERS.newRecord();
    record.setEmail(email);
    record.setPasswordHash(password_hash);
    record.setFullName(fullName);
    record.setIsActive(active);
  }

  public UsersDbO(UsersRecord record) {
    this.record = record;
  }
}
