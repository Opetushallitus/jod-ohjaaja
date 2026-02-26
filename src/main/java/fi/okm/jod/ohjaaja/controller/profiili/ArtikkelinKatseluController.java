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
import fi.okm.jod.ohjaaja.dto.SivuDto;
import fi.okm.jod.ohjaaja.service.ArtikkelinKatseluService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/artikkeli")
@RequiredArgsConstructor
@Tag(name = "artikkeli")
public class ArtikkelinKatseluController {
  private final ArtikkelinKatseluService service;

  @PostMapping("/katselu/{artikkeliErc}")
  @Operation(summary = "Adds a Katselu to the Artikkeli")
  void add(@AuthenticationPrincipal JodUser jodUser, @PathVariable String artikkeliErc) {
    service.add(jodUser, artikkeliErc);
  }

  @GetMapping("/katselu/katsotuimmat")
  @Operation(summary = "Gets the most viewed article Ercs")
  SivuDto<String> getMostViewedArtikkeliIds(
      @Nullable @RequestParam List<String> filterByArtikkeliErcs,
      @RequestParam(defaultValue = "12") int koko) {
    return service.findMostViewedArtikkeliErcs(
        filterByArtikkeliErcs, PageRequest.of(0, koko, Sort.by(Sort.Direction.DESC, "summa")));
  }

  @GetMapping("/viimeksi-katsellut")
  @Operation(summary = "Gets the last viewed article Ercs")
  SivuDto<String> getMostRecentViewedArtikkeliIds(
      @AuthenticationPrincipal JodUser jodUser, @RequestParam(defaultValue = "20") int koko) {
    return service.findMostRecentViewedArtikkeliErcsByUser(jodUser, Pageable.ofSize(koko));
  }
}
