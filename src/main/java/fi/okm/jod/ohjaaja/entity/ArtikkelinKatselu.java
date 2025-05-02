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
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
    indexes = {
      @Index(columnList = "artikkeliId"),
      @Index(columnList = "ohjaaja_id"),
      @Index(columnList = "anonyymiId")
    })
@AllArgsConstructor
@NoArgsConstructor
public class ArtikkelinKatselu {
  @GeneratedValue @Id private UUID id;

  @Column(updatable = false, nullable = false)
  private Instant luotu;

  @Column(updatable = false, nullable = false)
  private Long artikkeliId;

  @Column(updatable = false)
  private UUID ohjaajaId;

  @Column(updatable = false)
  private String anonyymiId;

  public ArtikkelinKatselu(
      Long artikkeliId, @Nullable UUID ohjaajaId, @Nullable String anonyymiId) {
    this.artikkeliId = artikkeliId;
    this.ohjaajaId = ohjaajaId;
    this.anonyymiId = anonyymiId;
    this.luotu = Instant.now();
  }
}
