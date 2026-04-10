package com.formulamatch.repository;

import com.formulamatch.entity.ChangeProposal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeProposalRepository extends JpaRepository<ChangeProposal, Long> {
}
