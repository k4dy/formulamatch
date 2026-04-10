package com.formulamatch.controller;

import com.formulamatch.dto.NewProductRequest;
import com.formulamatch.dto.ProposalRequest;
import com.formulamatch.dto.SubmissionResponse;
import com.formulamatch.service.SubmissionService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/submissions")
@Tag(name = "Crowdsource")
@SecurityRequirement(name = "X-Api-Key")
public class SubmissionController {

    private final SubmissionService service;

    public SubmissionController(SubmissionService service) {
        this.service = service;
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse submitProduct(@RequestBody @Valid NewProductRequest request) {
        return service.submitProduct(request);
    }

    @PostMapping("/products/{productId}/proposals")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse submitProposal(
            @Parameter(description = "ID of the existing product to correct") @PathVariable Integer productId,
            @RequestBody @Valid ProposalRequest request) {
        return service.submitProposal(productId, request);
    }

    @Hidden
    @GetMapping("/products/{id}/status")
    public SubmissionResponse getSubmissionStatus(@PathVariable Long id) {
        return service.getSubmissionStatus(id);
    }

    @Hidden
    @GetMapping("/proposals/{proposalId}/status")
    public SubmissionResponse getProposalStatus(@PathVariable("proposalId") Long id) {
        return service.getProposalStatus(id);
    }
}
