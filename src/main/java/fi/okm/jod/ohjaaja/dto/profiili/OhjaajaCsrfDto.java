/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.dto.profiili;

import fi.okm.jod.ohjaaja.dto.CsrfTokenDto;
import fi.okm.jod.ohjaaja.entity.TyoskentelyPaikka;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record OhjaajaCsrfDto(
    UUID id,
    String etunimi,
    String sukunimi,
    @NotNull CsrfTokenDto csrf,
    TyoskentelyPaikka tyoskentelyPaikka) {}
