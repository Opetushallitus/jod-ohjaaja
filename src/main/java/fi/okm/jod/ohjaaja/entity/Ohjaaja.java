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
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class Ohjaaja {

  @Id
  @Column(updatable = false, nullable = false)
  private UUID id;

  public Ohjaaja(UUID id) {
    this.id = id;
  }

  @Column
  @Enumerated(EnumType.STRING)
  @Setter
  private TyoskentelyPaikka tyoskentelyPaikka;

  @OneToMany(mappedBy = "ohjaaja", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private Set<OhjaajanSuosikki> suosikit;

  @OneToMany(mappedBy = "ohjaaja", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private Set<OhjaajanKiinnostus> kiinnostukset;

  @OneToMany(mappedBy = "ohjaaja", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private Set<ArtikkelinKommentti> kommentit;
}
