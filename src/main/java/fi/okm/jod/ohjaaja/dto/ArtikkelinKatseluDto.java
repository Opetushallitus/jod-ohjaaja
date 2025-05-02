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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.time.Instant;
import java.util.UUID;

public record ArtikkelinKatseluDto(
    @Null(groups = Add.class) UUID id,
    @NotNull Long artikkeliId,
    @Null(groups = Add.class) Instant luotu,
    String anonyymiId) {}
