package com.formulamatch.repository;

import com.formulamatch.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Integer> {

    Optional<Brand> findByNameIgnoreCase(String name);
}
