package com.eduflex.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eduflex.dto.user.CreateUserDTO.CreateUserRequest;
import com.eduflex.dto.user.CreateUserDTO.CreateUserResponse;
import com.eduflex.entity.user.UsersDbO;
import com.eduflex.repository.user.UserRepository;

@Service
public class RegisterUserUseCase {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public CreateUserResponse execute(CreateUserRequest request) {
    var user = userRepository.find_by_email(request.email());
    if (user != null) {
      return new CreateUserResponse(false, "Email existed!");
    }
    var passwordHash = passwordEncoder.encode(request.password());
    user = new UsersDbO(request.email(), passwordHash, request.name(), request.active());
    if (userRepository.save(user) == true) {
      return new CreateUserResponse(true, "Create new user successfully");
    } else {
      return new CreateUserResponse(false, "Failed to create new user");
    }
  }
}
