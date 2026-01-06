package com.logiflow.user.dto;

import com.logiflow.user.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Email(message = "Invalid email format")
        String email,

        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        Role role,

        Boolean enabled
) {}

