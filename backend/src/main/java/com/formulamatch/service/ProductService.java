package com.formulamatch.service;

import com.formulamatch.dto.*;
import com.formulamatch.entity.Product;
import com.formulamatch.exception.ResourceNotFoundException;
import com.formulamatch.repository.ProductIngredientRepository;
import com.formulamatch.repository.ProductRepository;
import com.formulamatch.repository.ProductSimilarityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductSimilarityRepository similarityRepository;
    private final ProductIngredientRepository ingredientRepository;

    public ProductService(ProductRepository productRepository,
                          ProductSimilarityRepository similarityRepository,
                          ProductIngredientRepository ingredientRepository) {
        this.productRepository = productRepository;
        this.similarityRepository = similarityRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public PagedResponse<ProductSummaryDto> searchByName(String query, Pageable pageable) {
        Page<Object[]> raw = productRepository.searchByName(query, pageable);
        Page<ProductSummaryDto> result = raw.map(row -> {
            int id = ((Number) row[0]).intValue();
            String name = (String) row[1];
            String brandName = (String) row[2];
            String imageUrl = (String) row[3];
            long ingredientCount = ((Number) row[4]).longValue();
            return new ProductSummaryDto(id, name, brandName, imageUrl, ingredientCount);
        });
        return new PagedResponse<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    public ProductDetailDto getById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("product not found: " + id));

        BrandDto brandDto = product.getBrand() != null
                ? new BrandDto(product.getBrand().getId(), product.getBrand().getName(), product.getBrand().getLogoUrl())
                : null;

        // build ingredient list
        List<IngredientDto> ingredients = new ArrayList<>();
        for (var pi : product.getIngredients()) {
            ingredients.add(new IngredientDto(
                    pi.getPosition(),
                    pi.getIngredient().getId(),
                    pi.getIngredient().getInciName(),
                    pi.getIngredient().getFunctions(),
                    pi.getConcentration()
            ));
        }

        return new ProductDetailDto(product.getId(), product.getName(), brandDto,
                product.getDescription(), product.getImageUrl(), product.getProductPageUrl(), ingredients);
    }

    public PagedResponse<SubstituteDto> getSubstitutes(Integer id, int top5Filter, Pageable pageable) {
        Page<Object[]> page;
        if (top5Filter == 0) {
            page = similarityRepository.findAllByProductId(id, pageable);
        } else {
            page = similarityRepository.findByProductIdAndTop5(id, top5Filter, pageable);
        }
        Page<SubstituteDto> mapped = page.map(row -> new SubstituteDto(
                ((Number) row[0]).intValue(),
                (String) row[1],
                (String) row[2],
                (String) row[3],
                ((Number) row[4]).intValue(),
                (Boolean) row[5],
                ((Number) row[6]).intValue(),
                ((Number) row[7]).longValue()
        ));
        return new PagedResponse<>(mapped.getContent(), mapped.getNumber(), mapped.getSize(), mapped.getTotalElements(), mapped.getTotalPages());
    }

    public Map<Integer, Long> getSubstituteCounts(Integer id) {
        Map<Integer, Long> counts = new LinkedHashMap<>();
        counts.put(5, 0L);
        counts.put(4, 0L);
        counts.put(3, 0L);
        counts.put(2, 0L);
        counts.put(1, 0L);
        counts.put(0, 0L);
        List<Object[]> rows = similarityRepository.countByTop5Matches(id);
        for (Object[] row : rows) {
            counts.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        return counts;
    }

    public Map<Integer, List<IngredientDto>> getTopIngredients(List<Integer> ids) {
        Map<Integer, List<IngredientDto>> result = new HashMap<>();
        List<Object[]> rows = ingredientRepository.findTop5ByProductIds(ids);
        for (Object[] row : rows) {
            Integer productId = ((Number) row[0]).intValue();
            if (!result.containsKey(productId)) {
                result.put(productId, new ArrayList<>());
            }
            result.get(productId).add(new IngredientDto(
                    ((Number) row[1]).intValue(),
                    ((Number) row[2]).intValue(),
                    (String) row[3],
                    null,
                    null
            ));
        }
        return result;
    }

}
