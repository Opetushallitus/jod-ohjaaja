/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service.profiili;

import fi.okm.jod.ohjaaja.dto.profiili.export.OhjaajaExportDto;
import fi.okm.jod.ohjaaja.dto.profiili.export.OhjaajanSuosikkiExportDto;
import fi.okm.jod.ohjaaja.entity.Ohjaaja;
import fi.okm.jod.ohjaaja.entity.OhjaajanSuosikki;
import java.util.stream.Collectors;

public final class ExportMapper {

  private ExportMapper() {}

  public static OhjaajaExportDto mapOhjaaja(Ohjaaja entity) {
    return entity == null
        ? null
        : new OhjaajaExportDto(
            entity.getId(),
            entity.getSuosikit().stream()
                .map(ExportMapper::mapOhjaajanSuosikki)
                .collect(Collectors.toSet()));
  }

  public static OhjaajanSuosikkiExportDto mapOhjaajanSuosikki(OhjaajanSuosikki entity) {
    return entity == null
        ? null
        : new OhjaajanSuosikkiExportDto(entity.getId(), entity.getLuotu(), entity.getArtikkeliId());
  }
}
