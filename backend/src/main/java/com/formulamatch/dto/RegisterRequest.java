package com.formulamatch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(hidden = true)
public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password
) {}
