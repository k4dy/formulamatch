package com.formulamatch.controller;

import com.formulamatch.dto.*;
import com.formulamatch.service.ProductService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products")
@SecurityRequirement(name = "X-Api-Key")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name or brand")
    public PagedResponse<ProductSummaryDto> search(
            @Parameter(description = "product name or brand", example = "nivea") @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.searchByName(q, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product details by ID")
    public ProductDetailDto getById(@PathVariable Integer id) {
        return productService.getById(id);
    }

    @GetMapping("/{id}/substitutes")
    @Operation(summary = "Get similar products by shared top-5 ingredients")
    public PagedResponse<SubstituteDto> getSubstitutes(
            @PathVariable Integer id,
            @Parameter(description = "min shared top-5 ingredients") @RequestParam(defaultValue = "5") int top5Filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productService.getSubstitutes(id, top5Filter, PageRequest.of(page, size));
    }

    @Hidden
    @GetMapping("/{id}/substitutes/counts")
    public Map<Integer, Long> getSubstituteCounts(@PathVariable Integer id) {
        return productService.getSubstituteCounts(id);
    }

    @Hidden
    @GetMapping("/top-ingredients")
    public Map<Integer, List<IngredientDto>> getTopIngredients(@RequestParam("ids") List<Integer> productIds) {
        return productService.getTopIngredients(productIds);
    }

}
