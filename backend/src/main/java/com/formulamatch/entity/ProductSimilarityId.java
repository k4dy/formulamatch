package com.formulamatch.entity;

import java.io.Serializable;
import java.util.Objects;

public class ProductSimilarityId implements Serializable {

    private Integer productId;
    private Integer similarProductId;

    public ProductSimilarityId() {}

    public ProductSimilarityId(Integer productId, Integer similarProductId) {
        this.productId = productId;
        this.similarProductId = similarProductId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductSimilarityId)) return false;
        ProductSimilarityId that = (ProductSimilarityId) o;
        return Objects.equals(productId, that.productId) &&
               Objects.equals(similarProductId, that.similarProductId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, similarProductId);
    }
}
