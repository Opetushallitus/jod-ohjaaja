/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.dto.profiili.export;

import fi.okm.jod.ohjaaja.entity.TyoskentelyPaikka;
import java.util.Set;
import java.util.UUID;

public record OhjaajaExportDto(
    UUID id,
    TyoskentelyPaikka tyoskentelyPaikka,
    Set<OhjaajanSuosikkiExportDto> suosikit,
    Set<OhjaajanKiinnostusExportDto> kiinnostukset) {}
