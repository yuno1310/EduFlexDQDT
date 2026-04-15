package com.eduflex.service;

import com.eduflex.dto.AdminDTO.DeleteUserResponse;
import com.eduflex.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteUserUseCase {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public DeleteUserResponse execute(UUID userId) {
        var user = userRepository.find_by_id(userId);
        if (user == null) {
            return new DeleteUserResponse(false, "User not found");
        }

        // Prevent deleting admin accounts
        String role = user.record.getRole();
        if ("admin".equals(role)) {
            return new DeleteUserResponse(false, "Cannot delete admin account");
        }

        boolean deleted = userRepository.deleteById(userId);
        if (deleted) {
            return new DeleteUserResponse(true, "User deleted successfully");
        } else {
            return new DeleteUserResponse(false, "Failed to delete user");
        }
    }
}
