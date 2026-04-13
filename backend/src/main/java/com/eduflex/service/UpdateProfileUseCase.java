package com.eduflex.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eduflex.dto.UpdateProfileDTO.UpdateProfileRequest;
import com.eduflex.dto.UpdateProfileDTO.UpdateProfileResponse;
import com.eduflex.generated.tables.records.UsersRecord;
import com.eduflex.repository.UserRepository;

@Service
public class UpdateProfileUseCase {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public UpdateProfileResponse execute(UUID userID, UpdateProfileRequest request) {
    UsersRecord record = userRepository.find_by_id_record(userID);

    if (record == null) {
      return new UpdateProfileResponse(false, "Failed to find user, user does not exist");
    }

    boolean isAdjust = false;
    if (request.newPassword() != null && !request.newPassword().trim().isEmpty()) {
      String hashedNewPassword = passwordEncoder.encode(request.newPassword());
      record.setPasswordHash(hashedNewPassword);
      isAdjust = true;
    }

    if (request.newFullName() != null && !request.newFullName().trim().isEmpty()) {
      record.setFullName(request.newFullName().trim());
      isAdjust = true;
    }

    if (isAdjust == true) {
      boolean success = userRepository.updateInfoUser(record);
      if (success == true) {
        return new UpdateProfileResponse(true, "Update User's Profile successfully");
      } else {
        return new UpdateProfileResponse(false, "Failed to update user's profile");
      }
    }
    return new UpdateProfileResponse(false, "Nothing to update");
  }
}
