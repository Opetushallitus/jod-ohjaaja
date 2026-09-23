/*
 * Copyright (c) 2026 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import javax.annotation.Nullable;

public record TilastotDto(
    @Nullable @Schema(description = "Start date (inclusive) of the time range, if given")
        LocalDate alku,
    @Nullable @Schema(description = "End date (inclusive) of the time range, if given")
        LocalDate loppu,
    @NotNull @Schema(description = "Current number of registered users") long kayttajienMaara,
    @NotNull @Schema(description = "Current distribution of users by workplace")
        List<TyoskentelyPaikkaMaaraDto> tyoskentelyPaikat,
    @NotNull @Schema(description = "Current distribution of user interests")
        List<KiinnostusMaaraDto> kiinnostukset,
    @NotNull
        @Schema(
            description =
                "Articles most often marked as favorite within the time range."
                    + " Favorites of deleted users are not included.")
        List<ArtikkeliMaaraDto> suosituimmatArtikkelit,
    @NotNull
        @Schema(
            description =
                "Most commented articles within the time range."
                    + " Comments removed by moderation are not included.")
        List<ArtikkeliMaaraDto> kommentoiduimmatArtikkelit,
    @NotNull @Schema(description = "Most viewed articles within the time range")
        List<ArtikkeliMaaraDto> katsotuimmatArtikkelit) {}
