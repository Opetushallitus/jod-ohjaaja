/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.controller;

import fi.okm.jod.ohjaaja.domain.JodUser;
import fi.okm.jod.ohjaaja.dto.ArtikkelinKommenttiDto;
import fi.okm.jod.ohjaaja.dto.KommentoidutArtikkelitDto;
import fi.okm.jod.ohjaaja.dto.SivuDto;
import fi.okm.jod.ohjaaja.dto.validationgroup.Add;
import fi.okm.jod.ohjaaja.service.ArtikkelinKommenttiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/artikkeli/kommentit")
@RequiredArgsConstructor
@Tag(name = "artikkeli/kommentit")
public class ArtikkelinKommenttiController {

  private final ArtikkelinKommenttiService service;

  @GetMapping
  @Operation(summary = "Finds all artikkelin kommentit")
  SivuDto<ArtikkelinKommenttiDto> findAllArtikkelinKommentit(
      @RequestParam String artikkeliErc, @RequestParam int sivu, @RequestParam int koko) {

    return service.findByArtikkeliErc(
        artikkeliErc, PageRequest.of(sivu, koko, Sort.by(Sort.Direction.DESC, "luotu")));
  }

  @GetMapping("/omat")
  @Operation(
      summary = "Finds all articles commented by the authenticated user with details",
      description = "Returns article ERCs with comment count and timestamp of latest comment")
  SivuDto<KommentoidutArtikkelitDto> findOmatKommentoidutArtikkelit(
      @AuthenticationPrincipal JodUser user, @RequestParam int sivu, @RequestParam int koko) {
    return service.findKommentoidutArtikkelit(
        user, PageRequest.of(sivu, koko, Sort.by(Sort.Direction.DESC, "uusinKommenttiAika")));
  }

  @PostMapping
  @Operation(summary = "Creates a new artikkelin kommentti")
  ArtikkelinKommenttiDto createArtikkelinKommentti(
      @AuthenticationPrincipal JodUser user,
      @Validated(Add.class) @RequestBody ArtikkelinKommenttiDto kommenttiDto) {
    return service.add(user, kommenttiDto.artikkeliErc(), kommenttiDto.kommentti());
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Deletes an artikkelin kommentti by ID")
  void deleteArtikkelinKommentti(@AuthenticationPrincipal JodUser user, @PathVariable UUID id) {
    service.delete(user, id);
  }

  @PostMapping("/{id}/ilmianto")
  public void ilmiannaKommentti(@AuthenticationPrincipal JodUser user, @PathVariable UUID id) {
    service.ilmianna(id, user);
  }
}
