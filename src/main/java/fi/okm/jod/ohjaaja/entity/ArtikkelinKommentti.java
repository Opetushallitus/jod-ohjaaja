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
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(indexes = {@Index(columnList = "ohjaaja_id"), @Index(columnList = "artikkeli_id")})
@AllArgsConstructor
@NoArgsConstructor
public class ArtikkelinKommentti {

  @Id
  @GeneratedValue
  @Column(updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private Ohjaaja ohjaaja;

  @Column(updatable = false, nullable = false)
  private Long artikkeliId;

  @Column(updatable = false, nullable = false)
  private Instant luotu;

  @Column(updatable = false, nullable = false, length = 2000)
  private String kommentti;

  @OneToMany(mappedBy = "artikkelinKommentti", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private Set<ArtikkelinKommentinIlmianto> ilmiannot;

  public ArtikkelinKommentti(Ohjaaja ohjaaja, Long artikkeliId, String kommentti) {
    this.ohjaaja = ohjaaja;
    this.artikkeliId = artikkeliId;
    this.kommentti = kommentti;
    this.luotu = Instant.now();
  }
}
