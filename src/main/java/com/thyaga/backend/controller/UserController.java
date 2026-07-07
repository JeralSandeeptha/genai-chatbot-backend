package com.thyaga.backend.controller;

import com.thyaga.backend.dto.CreateUserRequest;
import com.thyaga.backend.dto.UpdateUserRequest;
import com.thyaga.backend.dto.UserResponse;
import com.thyaga.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("POST /api/v1/users - creating user with email={}", request.email());
        try {
            UserResponse user = userService.createUser(request);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", user);
            responseBody.put("statusCode", HttpStatus.CREATED.value());
            responseBody.put("message", "Create user query was successful");

            log.info("Create user query was successful, userId={}", user.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        } catch (Exception ex) {
            log.error("Create user query internal server error", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Create user query internal server error",
                            "error", ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("GET /api/v1/users - fetching all users");
        try {
            List<UserResponse> userList = userService.getAllUsers();

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", userList);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Get all users query was successful");

            log.info("Get all users query was successful, count={}", userList.size());
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Get all users query internal server error", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Get all users query internal server error",
                            "error", ex.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable UUID id) {
        log.info("GET /api/v1/users/{} - fetching user", id);
        try {
            UserResponse user = userService.getUserById(id);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", user);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Get user query was successful");

            log.info("Get user query was successful, userId={}", id);
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Get user query internal server error, userId={}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Get user query internal server error",
                            "error", ex.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        log.info("PATCH /api/v1/users/{} - updating user", id);
        try {
            UserResponse user = userService.updateUser(id, request);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", user);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Update user query was successful");

            log.info("Update user query was successful, userId={}", id);
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Update user query internal server error, userId={}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Update user query internal server error",
                            "error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable UUID id) {
        log.info("DELETE /api/v1/users/{} - deleting user", id);
        try {
            userService.deleteUser(id);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", null);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Delete user query was successful");

            log.info("Delete user query was successful, userId={}", id);
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Delete user query internal server error, userId={}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Delete user query internal server error",
                            "error", ex.getMessage()));
        }
    }
}
