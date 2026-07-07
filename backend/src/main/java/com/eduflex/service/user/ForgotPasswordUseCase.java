package com.eduflex.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eduflex.dto.user.ForgotPasswordDTO.ForgotPasswordRequest;
import com.eduflex.dto.user.ForgotPasswordDTO.ForgotPasswordResponse;
import com.eduflex.repository.user.UserRepository;

@Service
public class ForgotPasswordUseCase {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ForgotPasswordResponse execute(ForgotPasswordRequest request) {
        if (request.email() == null || request.email().trim().isEmpty()) {
            return new ForgotPasswordResponse(false, "Email is required");
        }
        if (request.newPassword() == null || request.newPassword().trim().isEmpty()) {
            return new ForgotPasswordResponse(false, "New password is required");
        }

        var user = userRepository.find_by_email(request.email().trim());
        if (user == null) {
            return new ForgotPasswordResponse(false, "No account found with that email");
        }

        String hashed = passwordEncoder.encode(request.newPassword());
        boolean ok = userRepository.updatePasswordByEmail(request.email().trim(), hashed);
        if (ok) {
            return new ForgotPasswordResponse(true, "Password updated successfully");
        }
        return new ForgotPasswordResponse(false, "Failed to update password");
    }
}
