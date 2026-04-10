package com.formulamatch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cosing_substances")
public class CosingSubstance {

    @Id
    private Integer id;

    @Column(name = "inci_name")
    private String inciName;

    private String description;

    @Column(name = "cas_number")
    private String casNumber;

    @Column(name = "ec_number")
    private String ecNumber;

    @Column(name = "identified_ingredients")
    private String identifiedIngredients;

    @Column(name = "cosmetics_regulation_provisions")
    private String cosmeticsRegulationProvisions;

    private String functions;

    @Column(name = "sccs_opinions")
    private String sccsOpinions;

    private String url;

    public Integer getId() {
        return id;
    }

    public String getInciName() {
        return inciName;
    }

    public String getDescription() {
        return description;
    }

    public String getCasNumber() {
        return casNumber;
    }

    public String getEcNumber() {
        return ecNumber;
    }

    public String getFunctions() {
        return functions;
    }
}
