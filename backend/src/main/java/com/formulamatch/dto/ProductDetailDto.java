package com.formulamatch.dto;

import java.util.List;

public record ProductDetailDto(
        Integer id,
        String name,
        BrandDto brand,
        String description,
        String imageUrl,
        String productPageUrl,
        List<IngredientDto> ingredients
) {}
