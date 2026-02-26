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
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table
@IdClass(ArtikkelinKommentinIlmiantoId.class)
@AllArgsConstructor
@NoArgsConstructor
public class ArtikkelinKommentinIlmianto {
  @Id
  @Column(name = "artikkelin_kommentti_id", nullable = false)
  private UUID artikkelinKommenttiId;

  @ManyToOne(fetch = FetchType.LAZY)
  private ArtikkelinKommentti artikkelinKommentti;

  @Id
  @Column(name = "tunnistautunut", nullable = false)
  private boolean tunnistautunut;

  @Column(name = "maara", nullable = false)
  private int maara;

  @Column(name = "viimeksi_ilmiannettu", nullable = false)
  private Instant viimeksiIlmiannettu;
}
