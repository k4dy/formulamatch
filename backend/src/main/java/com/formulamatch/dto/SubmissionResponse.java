package com.formulamatch.dto;

import java.time.LocalDateTime;

public record SubmissionResponse(
        Long id,
        String status,
        LocalDateTime submittedAt
) {}
