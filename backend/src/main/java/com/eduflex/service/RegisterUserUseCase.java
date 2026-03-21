package com.eduflex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.CreateUserDTO.CreateUserRequest;
import com.eduflex.dto.CreateUserDTO.CreateUserResponse;
import com.eduflex.entity.UsersDbO;
import com.eduflex.repository.UserRepository;

import jakarta.validation.Valid;

@Service
public class RegisterUserUseCase {
  @Autowired
  private UserRepository userRepository;

  public CreateUserResponse execute(@Valid CreateUserRequest request) {
    var user = userRepository.find_by_email(request.email());
    if (user != null) {
      return new CreateUserResponse(false, "Email existed!");
    }
    user = new UsersDbO(request.email(), request.password(), request.name(), request.active());
    if (userRepository.save(user) == true) {
      return new CreateUserResponse(true, "Create new user successfully");
    } else {
      return new CreateUserResponse(false, "Failed to create new user");
    }
  }
}
