package com.formulamatch.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_submissions")
public class ProductSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "normalized_name", nullable = false)
    private String normalizedName;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "normalized_brand", nullable = false)
    private String normalizedBrand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "ingredient_fingerprint", nullable = false)
    private String ingredientFingerprint;

    @Column(name = "user_id")
    private Long userId;

    @OneToMany(mappedBy = "submission", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<SubmissionIngredient> ingredients = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getNormalizedBrand() {
        return normalizedBrand;
    }

    public void setNormalizedBrand(String normalizedBrand) {
        this.normalizedBrand = normalizedBrand;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public String getIngredientFingerprint() {
        return ingredientFingerprint;
    }

    public void setIngredientFingerprint(String ingredientFingerprint) {
        this.ingredientFingerprint = ingredientFingerprint;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<SubmissionIngredient> getIngredients() {
        return ingredients;
    }
}
