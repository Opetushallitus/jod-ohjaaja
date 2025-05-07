/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.controller.profiili;

import fi.okm.jod.ohjaaja.domain.JodUser;
import fi.okm.jod.ohjaaja.dto.ArtikkelinKatseluDto;
import fi.okm.jod.ohjaaja.dto.SivuDto;
import fi.okm.jod.ohjaaja.dto.validationgroup.Add;
import fi.okm.jod.ohjaaja.service.ArtikkelinKatseluService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/artikkeli/katselu")
@RequiredArgsConstructor
@Tag(name = "artikkeli/katselu")
public class ArtikkelinKatseluController {
  private final ArtikkelinKatseluService service;

  @PostMapping
  @Operation(summary = "Adds a Katselu to the Artikkeli")
  UUID add(
      @AuthenticationPrincipal JodUser jodUser,
      @Validated(Add.class) @RequestBody ArtikkelinKatseluDto artikkelinKatseluDto) {
    var ohjaajaId = jodUser == null ? null : jodUser.getId();
    return service.add(
        ohjaajaId, artikkelinKatseluDto.anonyymiId(), artikkelinKatseluDto.artikkeliId());
  }

  @GetMapping("/viimeksi-katsellut")
  @Operation(summary = "Gets the last viewed article ids")
  SivuDto<Long> getMostRecentViewedArtikkeliIds(
      @AuthenticationPrincipal JodUser jodUser, @RequestParam(defaultValue = "20") int koko) {
    return new SivuDto<>(
        service.findMostRecentViewedArtikkeliIdsByUser(jodUser, Pageable.ofSize(koko)));
  }
}
