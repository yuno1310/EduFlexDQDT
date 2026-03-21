package com.eduflex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduflex.dto.CreateUserDTO.CreateUserRequest;
import com.eduflex.dto.CreateUserDTO.CreateUserResponse;
import com.eduflex.service.RegisterUserUseCase;

@RestController
@RequestMapping("api/user")
public class UsersController {
  @Autowired
  private RegisterUserUseCase registerUserUseCase;

  @PostMapping("/register")
  public ResponseEntity<CreateUserResponse> createUser(CreateUserRequest request) {
    var response = registerUserUseCase.execute(request);
    if (response.success() == true) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }
}
