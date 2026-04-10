package com.formulamatch.repository;

import com.formulamatch.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query(value = """
            SELECT p.id, p.name, b.name AS brand_name, p.image_url,
                   COUNT(pi.id) AS ingredient_count
            FROM products p
            JOIN brands b ON p.brand_id = b.id
            LEFT JOIN product_ingredients pi ON pi.product_id = p.id
            WHERE LOWER(p.name) LIKE '%' || LOWER(:query) || '%'
               OR LOWER(b.name) LIKE '%' || LOWER(:query) || '%'
               OR LOWER(b.name || ' ' || p.name) LIKE '%' || LOWER(:query) || '%'
            GROUP BY p.id, p.name, b.name, p.image_url
            ORDER BY p.name
            """,
            countQuery = """
            SELECT COUNT(DISTINCT p.id)
            FROM products p
            JOIN brands b ON p.brand_id = b.id
            WHERE LOWER(p.name) LIKE '%' || LOWER(:query) || '%'
               OR LOWER(b.name) LIKE '%' || LOWER(:query) || '%'
               OR LOWER(b.name || ' ' || p.name) LIKE '%' || LOWER(:query) || '%'
            """,
            nativeQuery = true)
    Page<Object[]> searchByName(@Param("query") String query, Pageable pageable);
}
