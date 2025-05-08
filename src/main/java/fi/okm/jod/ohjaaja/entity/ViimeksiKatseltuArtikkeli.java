/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.entity;

import jakarta.persistence.*;
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
    uniqueConstraints = {@UniqueConstraint(columnNames = {"artikkeli_id", "ohjaaja_id"})})
@AllArgsConstructor
@NoArgsConstructor
public class ViimeksiKatseltuArtikkeli {

  @Id @GeneratedValue private Long id;

  @Column(name = "artikkeli_id", nullable = false)
  private Long artikkeliId;

  @Column(name = "ohjaaja_id", nullable = false)
  private UUID ohjaajaId;

  @Setter
  @Column(name = "viimeksi_katseltu", nullable = false)
  private Instant viimeksiKatseltu;

  public ViimeksiKatseltuArtikkeli(Long artikkeliId, UUID ohjaajaId) {
    this.artikkeliId = artikkeliId;
    this.ohjaajaId = ohjaajaId;
    this.viimeksiKatseltu = Instant.now();
  }
}
