package com.formulamatch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(hidden = true)
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {}
