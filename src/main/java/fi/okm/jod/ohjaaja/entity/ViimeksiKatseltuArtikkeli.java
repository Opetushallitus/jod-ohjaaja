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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(
    indexes = {@Index(columnList = "ohjaaja_id")},
    uniqueConstraints = {@UniqueConstraint(columnNames = {"artikkeli_erc", "ohjaaja_id"})})
@AllArgsConstructor
@NoArgsConstructor
public class ViimeksiKatseltuArtikkeli {

  @Id @GeneratedValue private Long id;

  @Column(name = "artikkeli_erc", nullable = false)
  private String artikkeliErc;

  @Column(name = "ohjaaja_id", nullable = false)
  private UUID ohjaajaId;

  @Setter
  @Column(name = "viimeksi_katseltu", nullable = false)
  private Instant viimeksiKatseltu;

  public ViimeksiKatseltuArtikkeli(String artikkeliErc, UUID ohjaajaId) {
    this.artikkeliErc = artikkeliErc;
    this.ohjaajaId = ohjaajaId;
    this.viimeksiKatseltu = Instant.now();
  }
}
