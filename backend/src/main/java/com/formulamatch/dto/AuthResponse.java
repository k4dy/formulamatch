package com.formulamatch.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(hidden = true)
public record AuthResponse(String apiKey) {}
