package com.formulamatch.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_seq")
    @SequenceGenerator(name = "products_seq", sequenceName = "products_id_seq", allocationSize = 1)
    private Integer id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "product_page_url")
    private String productPageUrl;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    private List<ProductIngredient> ingredients = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getProductPageUrl() {
        return productPageUrl;
    }

    public List<ProductIngredient> getIngredients() {
        return ingredients;
    }
}
