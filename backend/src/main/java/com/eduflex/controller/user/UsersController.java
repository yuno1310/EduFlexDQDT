package com.eduflex.controller.user;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduflex.dto.user.CreateUserDTO.CreateUserRequest;
import com.eduflex.dto.user.CreateUserDTO.CreateUserResponse;
import com.eduflex.dto.user.ForgotPasswordDTO.ForgotPasswordRequest;
import com.eduflex.dto.user.ForgotPasswordDTO.ForgotPasswordResponse;
import com.eduflex.dto.user.LogInDTO.LogInRequest;
import com.eduflex.dto.user.LogInDTO.LogInResponse;
import com.eduflex.dto.user.UpdateProfileDTO.UpdateProfileRequest;
import com.eduflex.dto.user.UpdateProfileDTO.UpdateProfileResponse;
import com.eduflex.service.user.ForgotPasswordUseCase;
import com.eduflex.service.user.LogInUseCase;
import com.eduflex.service.user.RegisterUserUseCase;
import com.eduflex.service.user.UpdateProfileUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/user")
public class UsersController {
    @Autowired
    private RegisterUserUseCase registerUserUseCase;

    @Autowired
    private LogInUseCase logInUseCase;

    @Autowired
    private UpdateProfileUseCase updateProfileUseCase;

    @Autowired
    private ForgotPasswordUseCase forgotPasswordUseCase;

    @PostMapping("/register")
    public ResponseEntity<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        var response = registerUserUseCase.execute(request);
        if (response.success()) return ResponseEntity.ok(response);
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LogInResponse> logInbyEmail(@Valid @RequestBody LogInRequest request) {
        var response = logInUseCase.execute(request);
        if (response.success()) return ResponseEntity.ok(response);
        return ResponseEntity.badRequest().body(response);
    }

    @PutMapping("/update-profile/{userID}")
    public ResponseEntity<UpdateProfileResponse> updateProfile(@PathVariable UUID userID,
            @Valid @RequestBody UpdateProfileRequest request) {
        var response = updateProfileUseCase.execute(userID, request);
        if (response.success()) return ResponseEntity.ok(response);
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        var response = forgotPasswordUseCase.execute(request);
        if (response.success()) return ResponseEntity.ok(response);
        return ResponseEntity.badRequest().body(response);
    }
}
