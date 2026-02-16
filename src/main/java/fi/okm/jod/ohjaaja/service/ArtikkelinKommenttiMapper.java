/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service;

import fi.okm.jod.ohjaaja.dto.ArtikkelinKommenttiDto;
import fi.okm.jod.ohjaaja.dto.KommentoidutArtikkelitDto;
import fi.okm.jod.ohjaaja.entity.ArtikkelinKommentti;
import fi.okm.jod.ohjaaja.entity.Ohjaaja;
import fi.okm.jod.ohjaaja.repository.projection.KommentitPerArtikkelit;
import java.util.Optional;

public final class ArtikkelinKommenttiMapper {

  private ArtikkelinKommenttiMapper() {}

  public static ArtikkelinKommenttiDto mapArtikkelinKommentti(ArtikkelinKommentti entity) {
    return entity == null
        ? null
        : new ArtikkelinKommenttiDto(
            entity.getId(),
            entity.getArtikkeliErc(),
            Optional.ofNullable(entity.getOhjaaja()).map(Ohjaaja::getId).orElse(null),
            entity.getKommentti(),
            entity.getLuotu());
  }

  public static KommentoidutArtikkelitDto mapKommentoidutArtikkelit(
      KommentitPerArtikkelit kommentitPerArtikkeli) {
    return kommentitPerArtikkeli == null
        ? null
        : new KommentoidutArtikkelitDto(
            kommentitPerArtikkeli.getArtikkeliErc(),
            kommentitPerArtikkeli.getUusinKommenttiAika(),
            kommentitPerArtikkeli.getVanhinKommenttiAika(),
            kommentitPerArtikkeli.getKommenttiMaara());
  }
}
