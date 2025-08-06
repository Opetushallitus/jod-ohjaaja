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
import fi.okm.jod.ohjaaja.entity.ArtikkelinKommentti;
import fi.okm.jod.ohjaaja.entity.Ohjaaja;
import java.util.Optional;

public final class ArtikkelinKommenttiMapper {

  private ArtikkelinKommenttiMapper() {}

  public static ArtikkelinKommenttiDto mapArtikkelinKommentti(ArtikkelinKommentti entity) {
    return entity == null
        ? null
        : new ArtikkelinKommenttiDto(
            entity.getId(),
            entity.getArtikkeliId(),
            Optional.ofNullable(entity.getOhjaaja()).map(Ohjaaja::getId).orElse(null),
            entity.getKommentti(),
            entity.getLuotu());
  }
}
