package com.formulamatch.repository;

import com.formulamatch.entity.ProductSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSubmissionRepository extends JpaRepository<ProductSubmission, Long> {
}
