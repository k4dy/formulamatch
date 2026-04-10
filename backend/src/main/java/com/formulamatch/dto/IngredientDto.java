package com.formulamatch.dto;

public record IngredientDto(
        Integer position,
        Integer cosingId,
        String inciName,
        String functions,
        String concentration
) {}
