package com.formulamatch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "submission_ingredients")
public class SubmissionIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private ProductSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cosing_id", nullable = false)
    private CosingSubstance substance;

    @Column(name = "raw_inci_name")
    private String rawInciName;

    @Column(nullable = false)
    private Integer position;

    public Long getId() {
        return id;
    }

    public ProductSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(ProductSubmission submission) {
        this.submission = submission;
    }

    public CosingSubstance getSubstance() {
        return substance;
    }

    public void setSubstance(CosingSubstance substance) {
        this.substance = substance;
    }

    public String getRawInciName() {
        return rawInciName;
    }

    public void setRawInciName(String rawInciName) {
        this.rawInciName = rawInciName;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
