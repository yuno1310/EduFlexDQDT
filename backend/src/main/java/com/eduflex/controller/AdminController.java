package com.eduflex.controller;

import com.eduflex.dto.AdminDTO.DeleteCourseResponse;
import com.eduflex.dto.AdminDTO.DeleteUserResponse;
import com.eduflex.dto.AdminDTO.GetAllUsersResponse;
import com.eduflex.service.DeleteCourseUseCase;
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

    @Autowired
    private DeleteCourseUseCase deleteCourseUseCase;

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

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<DeleteCourseResponse> deleteCourse(@PathVariable UUID courseId) {
        var response = deleteCourseUseCase.execute(courseId);
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
