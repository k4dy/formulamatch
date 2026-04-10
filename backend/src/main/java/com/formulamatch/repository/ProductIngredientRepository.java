package com.formulamatch.repository;

import com.formulamatch.entity.ProductIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, Integer> {

    @Query(value = "SELECT pi.product_id, pi.position, pi.ingredient_id, cs.inci_name FROM product_ingredients pi JOIN cosing_substances cs ON cs.id = pi.ingredient_id WHERE pi.product_id IN (:ids) AND pi.position <= 5 ORDER BY pi.product_id, pi.position",
            nativeQuery = true)
    List<Object[]> findTop5ByProductIds(@Param("ids") List<Integer> ids);
}
