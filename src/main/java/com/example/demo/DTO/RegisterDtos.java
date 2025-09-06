package com.example.demo.DTO;

import jakarta.validation.constraints.*;

public class RegisterDtos {
    public record RegisterRequest(
            @NotBlank(message = "Full name is required")
            String fullName,

            @Email(message = "Invalid email")
            @NotBlank(message = "Email is required")
            String email,

            @NotBlank @Size(min = 6, max = 100)
            String password,

            // Không bắt buộc
            @Pattern(regexp = "^[0-9+\\-()\\s]*$", message = "Invalid phone")
            String phone,

            // Nếu có mô hình gán người dùng vào store
            Long storeId
    ) {}

    public record UserResponse(
            Long id,
            String fullName,
            String email,
            String phone,
            String role,
            Boolean isActive
    ) {}
}
