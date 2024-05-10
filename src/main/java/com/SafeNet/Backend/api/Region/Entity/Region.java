package com.SafeNet.Backend.api.Region.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "region")
public class Region {
    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Long id;

    @NotNull
    @Column(name = "city")
    private String city;

    @NotNull
    @Column(name = "country")
    private String country;

    @NotNull
    @Column(name = "district")
    private String district;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
