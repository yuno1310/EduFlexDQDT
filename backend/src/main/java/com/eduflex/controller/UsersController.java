package com.eduflex.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduflex.dto.CreateUserDTO.CreateUserRequest;
import com.eduflex.dto.CreateUserDTO.CreateUserResponse;
import com.eduflex.dto.ForgotPasswordDTO.ForgotPasswordRequest;
import com.eduflex.dto.ForgotPasswordDTO.ForgotPasswordResponse;
import com.eduflex.dto.LogInDTO.LogInRequest;
import com.eduflex.dto.LogInDTO.LogInResponse;
import com.eduflex.dto.UpdateProfileDTO.UpdateProfileRequest;
import com.eduflex.dto.UpdateProfileDTO.UpdateProfileResponse;
import com.eduflex.service.ForgotPasswordUseCase;
import com.eduflex.service.LogInUseCase;
import com.eduflex.service.RegisterUserUseCase;
import com.eduflex.service.UpdateProfileUseCase;

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
