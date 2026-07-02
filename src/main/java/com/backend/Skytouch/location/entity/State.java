package com.backend.Skytouch.location.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "states")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class State {

    @Id
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(nullable = false)
    private String name;

    @Column(name = "state_code")
    private String stateCode;

    @Column(name = "has_cities", nullable = false)
    private boolean hasCities;

    private BigDecimal latitude;

    private BigDecimal longitude;
}
