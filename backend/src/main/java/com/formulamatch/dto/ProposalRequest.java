package com.formulamatch.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ProposalRequest(
        @NotEmpty @Valid List<IngredientEntry> ingredients
) {}
