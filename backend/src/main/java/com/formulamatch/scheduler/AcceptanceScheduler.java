package com.formulamatch.scheduler;

import com.formulamatch.entity.*;
import com.formulamatch.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AcceptanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(AcceptanceScheduler.class);

    private final ProductSubmissionRepository submissionRepository;
    private final ChangeProposalRepository proposalRepository;
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;

    public AcceptanceScheduler(ProductSubmissionRepository submissionRepository,
                               ChangeProposalRepository proposalRepository,
                               ProductRepository productRepository,
                               BrandRepository brandRepository) {
        this.submissionRepository = submissionRepository;
        this.proposalRepository = proposalRepository;
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
    }

    @Scheduled(fixedDelay = 3600000L)
    @Transactional
    public void processAll() {
        List<ProductSubmission> pendingSubmissions = submissionRepository.findAll()
                .stream()
                .filter(s -> s.getStatus() == SubmissionStatus.PENDING)
                .toList();

        Map<String, List<ProductSubmission>> submissionGroups = new HashMap<>();
        for (ProductSubmission s : pendingSubmissions) {
            String key = s.getNormalizedName() + "|" + s.getNormalizedBrand() + "|" + s.getIngredientFingerprint();
            if (!submissionGroups.containsKey(key)) {
                submissionGroups.put(key, new ArrayList<>());
            }
            submissionGroups.get(key).add(s);
        }
        for (List<ProductSubmission> group : submissionGroups.values()) {
            tryAcceptSubmissions(group);
        }

        List<ChangeProposal> pendingProposals = new ArrayList<>();
        for (ChangeProposal p : proposalRepository.findAll()) {
            if (p.getStatus() == SubmissionStatus.PENDING) {
                pendingProposals.add(p);
            }
        }

        Map<String, List<ChangeProposal>> proposalGroups = new HashMap<>();
        for (ChangeProposal p : pendingProposals) {
            String key = p.getProduct().getId() + "|" + p.getIngredientFingerprint();
            if (!proposalGroups.containsKey(key)) {
                proposalGroups.put(key, new ArrayList<>());
            }
            proposalGroups.get(key).add(p);
        }
        for (List<ChangeProposal> group : proposalGroups.values()) {
            tryAcceptProposals(group);
        }
    }

    private void tryAcceptSubmissions(List<ProductSubmission> group) {
        if (group.size() < 3) return;

        List<ProductSubmission> sorted = new ArrayList<>(group);
        sorted.sort(Comparator.comparing(ProductSubmission::getSubmittedAt));

        List<LocalDateTime> times = new ArrayList<>();
        for (ProductSubmission s : sorted) {
            times.add(s.getSubmittedAt());
        }
        if (!checkGaps(times)) return;

        acceptNewProduct(sorted.get(0), group);
    }

    private void acceptNewProduct(ProductSubmission representative, List<ProductSubmission> group) {
        Brand brand = brandRepository.findByNameIgnoreCase(representative.getBrandName())
                .orElseGet(() -> {
                    Brand b = new Brand();
                    b.setName(representative.getBrandName());
                    return brandRepository.save(b);
                });

        Product product = new Product();
        product.setName(representative.getProductName());
        product.setBrand(brand);
        productRepository.save(product);

        log.info("Auto-accepted new product '{}' by '{}' (id={})",
                product.getName(), brand.getName(), product.getId());

        for (ProductSubmission s : group) {
            s.setStatus(SubmissionStatus.ACCEPTED);
        }
    }

    private void tryAcceptProposals(List<ChangeProposal> group) {
        if (group.size() < 3) return;

        List<ChangeProposal> sorted = new ArrayList<>(group);
        sorted.sort(Comparator.comparing(ChangeProposal::getSubmittedAt));

        List<LocalDateTime> times = new ArrayList<>();
        for (ChangeProposal p : sorted) {
            times.add(p.getSubmittedAt());
        }
        if (!checkGaps(times)) return;

        for (ChangeProposal p : group) {
            p.setStatus(SubmissionStatus.ACCEPTED);
        }
        log.info("Auto-accepted change proposal for product id={}", group.get(0).getProduct().getId());
    }

    boolean checkGaps(List<LocalDateTime> times) {
        for (int i = 1; i < times.size(); i++) {
            if (Duration.between(times.get(i - 1), times.get(i)).toHours() < 2) return false;
        }
        return true;
    }
}
