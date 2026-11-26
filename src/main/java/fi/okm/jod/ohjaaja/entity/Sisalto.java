/*
 * Copyright (c) 2026 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Type of a CMS content item, identified by its external reference code (ERC).
 *
 * <p>Other tables do not reference this table; an ERC without a row is treated as {@link
 * SisaltoTyyppi#ARTIKKELI}.
 */
@Entity
@Getter
@NoArgsConstructor
public class Sisalto {

  @Id
  @Column(nullable = false, updatable = false)
  private String erc;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SisaltoTyyppi sisaltotyyppi;

  @Column(nullable = false)
  private Instant paivitetty;
}
