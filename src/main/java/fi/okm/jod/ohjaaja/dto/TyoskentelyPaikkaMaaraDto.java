/*
 * Copyright (c) 2026 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.dto;

import fi.okm.jod.ohjaaja.entity.TyoskentelyPaikka;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import javax.annotation.Nullable;

public record TyoskentelyPaikkaMaaraDto(
    @Nullable @Schema(description = "Workplace, null if the user has not set it")
        TyoskentelyPaikka tyoskentelyPaikka,
    @NotNull @Schema(description = "Number of users") long maara) {}
