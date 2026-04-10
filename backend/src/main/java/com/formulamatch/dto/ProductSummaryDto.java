package com.formulamatch.dto;

public record ProductSummaryDto(
        Integer id,
        String name,
        String brandName,
        String imageUrl,
        Long ingredientCount
) {}
