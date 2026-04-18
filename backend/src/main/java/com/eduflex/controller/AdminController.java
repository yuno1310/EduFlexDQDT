package com.eduflex.controller;

import com.eduflex.dto.AdminDTO.DeleteUserResponse;
import com.eduflex.dto.AdminDTO.GetAllUsersResponse;
import com.eduflex.service.DeleteUserUseCase;
import com.eduflex.service.GetAllUsersUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private GetAllUsersUseCase getAllUsersUseCase;

    @Autowired
    private DeleteUserUseCase deleteUserUseCase;

    @GetMapping("/users")
    public ResponseEntity<GetAllUsersResponse> getAllUsers() {
        return ResponseEntity.ok(getAllUsersUseCase.execute());
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<DeleteUserResponse> deleteUser(@PathVariable UUID userId) {
        var response = deleteUserUseCase.execute(userId);
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
