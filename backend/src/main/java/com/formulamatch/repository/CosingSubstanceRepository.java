package com.formulamatch.repository;

import com.formulamatch.entity.CosingSubstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CosingSubstanceRepository extends JpaRepository<CosingSubstance, Integer> {

    List<CosingSubstance> findByInciNameIgnoreCaseIn(List<String> inciNames);
}
