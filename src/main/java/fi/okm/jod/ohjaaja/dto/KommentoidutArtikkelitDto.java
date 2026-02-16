/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record KommentoidutArtikkelitDto(
    @NotNull String artikkeliErc,
    @NotNull @Schema(description = "Timestamp of the most recent comment")
        Instant uusinKommenttiAika,
    @NotNull @Schema(description = "Timestamp of the oldest comment") Instant vanhinKommenttiAika,
    @NotNull @Schema(description = "Number of comments on this article") int kommenttiMaara) {}
