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
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table
@IdClass(ArtikkelinKatseluId.class)
@AllArgsConstructor
@NoArgsConstructor
public class ArtikkelinKatselu {
  @Id
  @Column(name = "artikkeli_id", nullable = false)
  private Long artikkeliId;

  @Id
  @Column(name = "paiva", nullable = false)
  private LocalDate paiva;

  @Column(name = "maara", nullable = false)
  private int maara;
}
