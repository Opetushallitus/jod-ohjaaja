/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.dto;

import fi.okm.jod.ohjaaja.dto.validationgroup.Add;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record ArtikkelinKommenttiDto(
    @Null(groups = Add.class) @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID id,
    @NotNull String artikkeliErc,
    @Null(groups = Add.class) @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID ohjaajaId,
    @NotNull @NotBlank @Size(max = 2000) String kommentti,
    @Null(groups = Add.class) @Schema(accessMode = Schema.AccessMode.READ_ONLY) Instant luotu) {}
