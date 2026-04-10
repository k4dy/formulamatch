package com.formulamatch.service;

import com.formulamatch.dto.IngredientEntry;
import com.formulamatch.dto.NewProductRequest;
import com.formulamatch.dto.ProposalRequest;
import com.formulamatch.dto.SubmissionResponse;
import com.formulamatch.entity.*;
import com.formulamatch.exception.ResourceNotFoundException;
import com.formulamatch.exception.UnresolvableInciException;
import com.formulamatch.repository.ChangeProposalRepository;
import com.formulamatch.repository.CosingSubstanceRepository;
import com.formulamatch.repository.ProductRepository;
import com.formulamatch.repository.ProductSubmissionRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SubmissionService {

    private final ProductSubmissionRepository submissionRepository;
    private final ChangeProposalRepository proposalRepository;
    private final CosingSubstanceRepository cosingRepository;
    private final ProductRepository productRepository;

    public SubmissionService(ProductSubmissionRepository submissionRepository,
                             ChangeProposalRepository proposalRepository,
                             CosingSubstanceRepository cosingRepository,
                             ProductRepository productRepository) {
        this.submissionRepository = submissionRepository;
        this.proposalRepository = proposalRepository;
        this.cosingRepository = cosingRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public SubmissionResponse submitProduct(NewProductRequest request) {
        Map<String, CosingSubstance> byName = resolveIngredientMap(request.ingredients());

        ProductSubmission submission = new ProductSubmission();
        submission.setProductName(request.productName());
        submission.setNormalizedName(normalize(request.productName()));
        submission.setBrandName(request.brandName());
        submission.setNormalizedBrand(normalize(request.brandName()));
        submission.setIngredientFingerprint(buildFingerprint(byName.values()));
        submission.setUserId(currentUserId());

        List<IngredientEntry> sorted = new ArrayList<>(request.ingredients());
        sorted.sort(Comparator.comparingInt(IngredientEntry::position));

        for (IngredientEntry req : sorted) {
            SubmissionIngredient si = new SubmissionIngredient();
            si.setSubmission(submission);
            si.setSubstance(byName.get(req.inciName().toLowerCase()));
            si.setRawInciName(req.rawInciName());
            si.setPosition(req.position());
            submission.getIngredients().add(si);
        }

        submissionRepository.save(submission);
        return new SubmissionResponse(submission.getId(), submission.getStatus().name(), submission.getSubmittedAt());
    }

    @Transactional
    public SubmissionResponse submitProposal(Integer productId, ProposalRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        Map<String, CosingSubstance> byName = resolveIngredientMap(request.ingredients());

        ChangeProposal proposal = new ChangeProposal();
        proposal.setProduct(product);
        proposal.setIngredientFingerprint(buildFingerprint(byName.values()));
        proposal.setUserId(currentUserId());

        List<IngredientEntry> sorted = new ArrayList<>(request.ingredients());
        sorted.sort(Comparator.comparingInt(IngredientEntry::position));

        for (IngredientEntry req : sorted) {
            ProposalIngredient pi = new ProposalIngredient();
            pi.setProposal(proposal);
            pi.setSubstance(byName.get(req.inciName().toLowerCase()));
            pi.setRawInciName(req.rawInciName());
            pi.setPosition(req.position());
            proposal.getIngredients().add(pi);
        }

        proposalRepository.save(proposal);
        return new SubmissionResponse(proposal.getId(), proposal.getStatus().name(), proposal.getSubmittedAt());
    }

    @Transactional
    public SubmissionResponse getSubmissionStatus(Long id) {
        ProductSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + id));
        return new SubmissionResponse(submission.getId(), submission.getStatus().name(), submission.getSubmittedAt());
    }

    @Transactional
    public SubmissionResponse getProposalStatus(Long id) {
        ChangeProposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found: " + id));
        return new SubmissionResponse(proposal.getId(), proposal.getStatus().name(), proposal.getSubmittedAt());
    }

    private Map<String, CosingSubstance> resolveIngredientMap(List<IngredientEntry> entries) {
        List<String> names = new ArrayList<>();
        for (IngredientEntry e : entries) {
            names.add(e.inciName());
        }
        Map<String, CosingSubstance> map = new HashMap<>();
        for (CosingSubstance s : resolveSubstances(names)) {
            map.put(s.getInciName().toLowerCase(), s);
        }
        return map;
    }

    private List<CosingSubstance> resolveSubstances(List<String> inciNames) {
        List<CosingSubstance> found = cosingRepository.findByInciNameIgnoreCaseIn(inciNames);
        Set<String> foundNames = new HashSet<>();
        for (CosingSubstance s : found) {
            foundNames.add(s.getInciName().toLowerCase());
        }
        List<String> unknown = new ArrayList<>();
        for (String n : inciNames) {
            if (!foundNames.contains(n.toLowerCase())) {
                unknown.add(n);
            }
        }
        if (!unknown.isEmpty()) throw new UnresolvableInciException(unknown);
        return found;
    }

    private String buildFingerprint(Collection<CosingSubstance> substances) {
        List<String> ids = new ArrayList<>();
        for (CosingSubstance s : substances) {
            ids.add(String.valueOf(s.getId()));
        }
        Collections.sort(ids);
        return String.join(",", ids);
    }

    private String normalize(String value) {
        return value.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof UsernamePasswordAuthenticationToken token
                && token.getPrincipal() instanceof User user) {
            return user.getId();
        }
        return null;
    }
}
