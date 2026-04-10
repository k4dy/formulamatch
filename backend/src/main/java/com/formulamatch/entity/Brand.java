package com.formulamatch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "brands")
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "brands_seq")
    @SequenceGenerator(name = "brands_seq", sequenceName = "brands_id_seq", allocationSize = 1)
    private Integer id;

    private String name;
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "official_website_url")
    private String officialWebsiteUrl;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getOfficialWebsiteUrl() {
        return officialWebsiteUrl;
    }
}
