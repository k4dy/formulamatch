package com.formulamatch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_ingredients")
public class ProductIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private CosingSubstance ingredient;

    @Column(nullable = false)
    private Integer position;

    private String concentration;

    public Integer getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public CosingSubstance getIngredient() {
        return ingredient;
    }

    public Integer getPosition() {
        return position;
    }

    public String getConcentration() {
        return concentration;
    }
}
