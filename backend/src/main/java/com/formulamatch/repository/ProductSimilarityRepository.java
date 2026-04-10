package com.formulamatch.repository;

import com.formulamatch.entity.ProductSimilarity;
import com.formulamatch.entity.ProductSimilarityId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductSimilarityRepository extends JpaRepository<ProductSimilarity, ProductSimilarityId> {

    @Query(value = """
            SELECT ps.similar_product_id,
                   p.name,
                   b.name AS brand_name,
                   p.image_url,
                   ps.number_of_matches,
                   ps.is_golden_match,
                   ps.top5_matches,
                   (SELECT COUNT(*) FROM product_ingredients pi WHERE pi.product_id = ps.similar_product_id) AS ingredient_count
            FROM product_similarities ps
            JOIN products p ON p.id = ps.similar_product_id
            JOIN brands b ON b.id = p.brand_id
            WHERE ps.product_id = :productId
              AND ps.top5_matches = :top5Filter
            ORDER BY ps.number_of_matches DESC
            """,
            countQuery = "SELECT COUNT(*) FROM product_similarities WHERE product_id = :productId AND top5_matches = :top5Filter",
            nativeQuery = true)
    Page<Object[]> findByProductIdAndTop5(@Param("productId") Integer productId, @Param("top5Filter") Integer top5Filter, Pageable pageable);

    @Query(value = "SELECT ps.similar_product_id, p.name, b.name AS brand_name, p.image_url, ps.number_of_matches, ps.is_golden_match, ps.top5_matches, (SELECT COUNT(*) FROM product_ingredients pi WHERE pi.product_id = ps.similar_product_id) AS ingredient_count FROM product_similarities ps JOIN products p ON p.id = ps.similar_product_id JOIN brands b ON b.id = p.brand_id WHERE ps.product_id = :productId ORDER BY ps.number_of_matches DESC",
            countQuery = "SELECT COUNT(*) FROM product_similarities WHERE product_id = :productId",
            nativeQuery = true)
    Page<Object[]> findAllByProductId(@Param("productId") Integer productId, Pageable pageable);

    @Query(value = """
            SELECT ps.top5_matches, COUNT(*) AS cnt
            FROM product_similarities ps
            WHERE ps.product_id = :productId
            GROUP BY ps.top5_matches
            ORDER BY ps.top5_matches DESC
            """,
            nativeQuery = true)
    List<Object[]> countByTop5Matches(@Param("productId") Integer productId);
}
