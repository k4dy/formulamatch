package com.formulamatch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "proposal_ingredients")
public class ProposalIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private ChangeProposal proposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cosing_id", nullable = false)
    private CosingSubstance substance;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "raw_inci_name")
    private String rawInciName;

    public Long getId() {
        return id;
    }

    public ChangeProposal getProposal() {
        return proposal;
    }

    public void setProposal(ChangeProposal proposal) {
        this.proposal = proposal;
    }

    public CosingSubstance getSubstance() {
        return substance;
    }

    public void setSubstance(CosingSubstance substance) {
        this.substance = substance;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getRawInciName() {
        return rawInciName;
    }

    public void setRawInciName(String rawInciName) {
        this.rawInciName = rawInciName;
    }
}
