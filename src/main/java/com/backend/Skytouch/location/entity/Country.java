package com.backend.Skytouch.location.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "countries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Country {

    @Id
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String iso3;

    private String iso2;

    @Column(name = "numeric_code")
    private String numericCode;

    @Column(name = "phone_code")
    private String phoneCode;

    private String capital;

    private String currency;

    @Column(name = "currency_name")
    private String currencyName;

    @Column(name = "currency_symbol")
    private String currencySymbol;

    private String tld;

    @Column(name = "native")
    private String nativeName;

    private String region;

    private String subregion;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String emoji;

    @Column(name = "has_states", nullable = false)
    private boolean hasStates;
}
