package com.ecommerce.user.controller.admin;

import com.ecommerce.user.dto.UserRequestDTO;
import com.ecommerce.user.dto.UserResponseDTO;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.mapper.UserMapper;
import com.ecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - User Management", description = "Endpoints for administrators to manage all users")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final UserService userService;
    private final UserMapper userMapper;


    @Operation(summary = "Get all registered users (admin only)")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.fetchAllUsers()
                .stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }


    @Operation(summary = "Get a specific user by ID (admin only)")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String id) {
        User user = userService.fetchUserById(id);
        return ResponseEntity.ok(userMapper.toResponseDTO(user));
    }


    @Operation(summary = "Create a new user manually (admin only)")
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO dto) {
        User user = userMapper.toEntity(dto);
        User created = userService.createUserByAdmin(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponseDTO(created));
    }

    @Operation(summary = "Update any user by ID (admin only)")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable String id,
                                                      @Valid @RequestBody UserRequestDTO dto) {
        User userToUpdate = userMapper.toEntity(dto);
        User updated = userService.updateUserByAdmin(id, userToUpdate);
        return ResponseEntity.ok(userMapper.toResponseDTO(updated));
    }


    @Operation(summary = "Delete any user by ID (admin only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String id) {
        String message = userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
