package com.formulamatch.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NewProductRequest(
        @NotBlank @Size(max = 500) String productName,
        @NotBlank @Size(max = 255) String brandName,
        @NotEmpty @Valid List<IngredientEntry> ingredients
) {}
