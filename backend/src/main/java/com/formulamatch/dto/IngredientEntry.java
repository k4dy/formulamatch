package com.formulamatch.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record IngredientEntry(
        @NotBlank String inciName,
        String rawInciName,
        @Min(1) Integer position
) {}
