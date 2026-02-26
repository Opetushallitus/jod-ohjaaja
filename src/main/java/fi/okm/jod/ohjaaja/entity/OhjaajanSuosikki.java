/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(indexes = {@Index(columnList = "ohjaaja_id")})
@AllArgsConstructor
@NoArgsConstructor
public class OhjaajanSuosikki {

  @GeneratedValue @Id private UUID id;

  @Column(updatable = false, nullable = false)
  private Instant luotu;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Ohjaaja ohjaaja;

  @Column(name = "artikkeli_erc", updatable = false, nullable = false)
  private String artikkeliErc;

  public OhjaajanSuosikki(String artikkeliErc, Ohjaaja ohjaaja) {
    this.artikkeliErc = artikkeliErc;
    this.ohjaaja = ohjaaja;
    this.luotu = Instant.now();
  }
}
