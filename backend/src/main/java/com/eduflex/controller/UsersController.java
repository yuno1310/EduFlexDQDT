package com.eduflex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduflex.dto.CreateUserDTO.CreateUserRequest;
import com.eduflex.dto.CreateUserDTO.CreateUserResponse;
import com.eduflex.dto.LogInDTO.LogInRequest;
import com.eduflex.dto.LogInDTO.LogInResponse;
import com.eduflex.service.LogInUseCase;
import com.eduflex.service.RegisterUserUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/user")
public class UsersController {
  @Autowired
  private RegisterUserUseCase registerUserUseCase;

  @Autowired
  private LogInUseCase logInUseCase;

  @PostMapping("/register")
  public ResponseEntity<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
    var response = registerUserUseCase.execute(request);
    if (response.success() == true) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }

  @PostMapping("/login")
  public ResponseEntity<LogInResponse> logInbyEmail(@Valid @RequestBody LogInRequest request) {
    var response = logInUseCase.execute(request);
    if (response.success() == true) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }
}
