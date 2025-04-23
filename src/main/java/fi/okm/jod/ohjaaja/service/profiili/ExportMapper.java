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
import fi.okm.jod.ohjaaja.entity.Ohjaaja;

public final class ExportMapper {

  private ExportMapper() {}

  public static OhjaajaExportDto mapOhjaaja(Ohjaaja entity) {
    return entity == null ? null : new OhjaajaExportDto(entity.getId());
  }
}
