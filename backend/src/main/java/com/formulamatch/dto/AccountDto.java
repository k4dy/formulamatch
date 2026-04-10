package com.formulamatch.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(hidden = true)
public record AccountDto(String email, String apiKey) {}
