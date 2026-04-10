package com.formulamatch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_similarities")
@IdClass(ProductSimilarityId.class)
public class ProductSimilarity {

    @Id
    @Column(name = "product_id")
    private Integer productId;

    @Id
    @Column(name = "similar_product_id")
    private Integer similarProductId;

    @Column(name = "number_of_matches", nullable = false)
    private Integer numberOfMatches;

    @Column(name = "is_golden_match", nullable = false)
    private Boolean isGoldenMatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "similar_product_id", insertable = false, updatable = false)
    private Product similarProduct;

    public Integer getProductId() {
        return productId;
    }

    public Integer getSimilarProductId() {
        return similarProductId;
    }

    public Integer getNumberOfMatches() {
        return numberOfMatches;
    }

    public Boolean getIsGoldenMatch() {
        return isGoldenMatch;
    }

    public Product getSimilarProduct() {
        return similarProduct;
    }
}
