/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.dto.profiili;

import fi.okm.jod.ohjaaja.dto.validationgroup.Add;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Null;
import java.time.Instant;
import java.util.UUID;

public record KiinnostusDto(
    @Null(groups = Add.class) @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID id,
    long asiasanaId,
    @Null(groups = Add.class) @Schema(accessMode = Schema.AccessMode.READ_ONLY) Instant luotu) {}
