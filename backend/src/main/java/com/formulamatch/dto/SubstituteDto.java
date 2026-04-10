package com.formulamatch.dto;

public record SubstituteDto(
        Integer productId,
        String productName,
        String brandName,
        String imageUrl,
        Integer matchCount,
        Boolean isGoldenMatch,
        Integer top5Matches,
        Long ingredientCount
) {}
