package com.registration.service;

import com.registration.dto.*;
import com.registration.entity.JwtToken;
import com.registration.entity.User;
import com.registration.exception.BadRequestException;
import com.registration.exception.ConflictException;
import com.registration.exception.ResourceNotFoundException;
import com.registration.exception.UnauthorizedException;
import com.registration.repository.JwtTokenRepository;
import com.registration.repository.UserRepository;
import com.registration.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenRepository jwtTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final Map<String, OtpEntry> otpStore = new HashMap<>();

    public AuthService(UserRepository userRepository,
                       JwtTokenRepository jwtTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtTokenRepository = jwtTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    private static class OtpEntry {
        String otp;
        LocalDateTime createdAt;
    }

    private String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private boolean isOtpExpired(LocalDateTime createdAt) {
        return createdAt.isBefore(LocalDateTime.now().minusMinutes(5));
    }

    public ApiResponse register(RegisterRequest request) {
        if (userRepository.existsByUserName(request.getUsername())) {
            throw new ConflictException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        User user = new User();
        user.setUserName(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.CUSTOMER);

        userRepository.save(user);

        return new ApiResponse(true, "User registered successfully");
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid Email or Password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid Email or Password");
        }

        String jwtToken = jwtService.generateToken(user);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = now.plusHours(24);

        JwtToken tokenEntity = new JwtToken();
        tokenEntity.setUserId(user.getUserId());
        tokenEntity.setToken(jwtToken);
        tokenEntity.setCreatedAt(now);
        tokenEntity.setExpiresAt(expiryTime);
        jwtTokenRepository.save(tokenEntity);

        return new LoginResponse(
                jwtToken,
                "Bearer",
                user.getUserId(),
                user.getUserName(),
                user.getRole().name()
        );
    }

    @Transactional
    public ApiResponse logout(String token) {
        String cleanToken = token.replace("Bearer ", "");
        jwtTokenRepository.findByToken(cleanToken)
                .ifPresent(jwtTokenRepository::delete);
        return new ApiResponse(true, "Logout successful");
    }

    public ApiResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this email"));

        String otp = generateOtp();
        OtpEntry entry = new OtpEntry();
        entry.otp = otp;
        entry.createdAt = LocalDateTime.now();
        otpStore.put(request.getEmail(), entry);

        System.out.println("OTP for " + request.getEmail() + ": " + otp);

        return new ApiResponse(true, "OTP Sent Successfully");
    }

    public ApiResponse verifyOtp(VerifyOtpRequest request) {
        OtpEntry entry = otpStore.get(request.getEmail());
        if (entry == null) {
            throw new BadRequestException("OTP not found. Please request a new OTP.");
        }
        if (isOtpExpired(entry.createdAt)) {
            otpStore.remove(request.getEmail());
            throw new BadRequestException("OTP has expired. Please request a new OTP.");
        }
        if (!entry.otp.equals(request.getOtp())) {
            throw new BadRequestException("Invalid OTP");
        }
        return new ApiResponse(true, "OTP verified successfully");
    }

    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest request) {
        OtpEntry entry = otpStore.get(request.getEmail());
        if (entry == null) {
            throw new BadRequestException("OTP not found. Please request a new OTP.");
        }
        if (isOtpExpired(entry.createdAt)) {
            otpStore.remove(request.getEmail());
            throw new BadRequestException("OTP has expired. Please request a new OTP.");
        }
        if (!entry.otp.equals(request.getOtp())) {
            throw new BadRequestException("Invalid OTP");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpStore.remove(request.getEmail());
        jwtTokenRepository.deleteByUserId(user.getUserId());

        return new ApiResponse(true, "Password reset successfully");
    }

    @Transactional
    public ApiResponse changePassword(ChangePasswordRequest request, User user) {
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new UnauthorizedException("Old password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        jwtTokenRepository.deleteByUserId(user.getUserId());

        return new ApiResponse(true, "Password changed successfully");
    }
}
