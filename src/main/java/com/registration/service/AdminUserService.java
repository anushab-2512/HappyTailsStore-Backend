package com.registration.service;

import com.registration.dto.AdminUpdateUserRequest;
import com.registration.dto.AdminUserResponse;
import com.registration.entity.User;
import com.registration.exception.BadRequestException;
import com.registration.exception.ConflictException;
import com.registration.exception.ResourceNotFoundException;
import com.registration.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "userId"))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AdminUserResponse updateUser(Integer userId, AdminUpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            if (userRepository.existsByUserName(request.getUsername())
                    && !request.getUsername().equals(user.getUserName())) {
                throw new ConflictException("Username already taken");
            }
            user.setUserName(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (userRepository.existsByEmail(request.getEmail())
                    && !request.getEmail().equals(user.getEmail())) {
                throw new ConflictException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getRole() != null) {
            if (request.getRole() != User.Role.ADMIN && request.getRole() != User.Role.CUSTOMER) {
                throw new BadRequestException("Role must be ADMIN or CUSTOMER");
            }
            user.setRole(request.getRole());
        }

        userRepository.save(user);
        return toResponse(user);
    }

    private AdminUserResponse toResponse(User user) {
        String createdAt = user.getCreatedAt() != null ? user.getCreatedAt().format(FORMATTER) : null;
        return new AdminUserResponse(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getRole().name(),
                createdAt
        );
    }
}