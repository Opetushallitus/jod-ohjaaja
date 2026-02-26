/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.controller;

import fi.okm.jod.ohjaaja.dto.ArtikkelinKommenttiDto;
import fi.okm.jod.ohjaaja.dto.SivuDto;
import fi.okm.jod.ohjaaja.repository.projection.IlmiantoYhteenveto;
import fi.okm.jod.ohjaaja.service.ArtikkelinKommenttiModerointiService;
import fi.okm.jod.ohjaaja.service.ArtikkelinKommenttiService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal-api/moderointi/kommentit")
@RequiredArgsConstructor
@Tag(name = "internal/moderointi/kommentit")
public class ArtikkelinKommenttiModerointiController {
  private final ArtikkelinKommenttiModerointiService service;
  private final ArtikkelinKommenttiService artikkelinKommenttiService;

  @GetMapping
  public SivuDto<ArtikkelinKommenttiDto> getKommentit(
      @RequestParam int sivu, @RequestParam int koko) {
    return artikkelinKommenttiService.findAll(
        PageRequest.of(sivu, koko, Sort.by(Sort.Direction.DESC, "luotu")));
  }

  @GetMapping("/ilmiannot")
  public List<IlmiantoYhteenveto> getIlmiannot() {
    return service.getAllIlmiantoYhteenveto();
  }

  @DeleteMapping("/{artikkelinKommenttiId}")
  public void deleteArtikkelinKommentti(@PathVariable UUID artikkelinKommenttiId) {
    service.deleteArtikkelinKommentti(artikkelinKommenttiId);
  }

  @DeleteMapping("/ilmiannot/{artikkelinKommenttiId}")
  public void deleteIlmiannot(@PathVariable UUID artikkelinKommenttiId) {
    service.deleteIlmiannot(artikkelinKommenttiId);
  }
}
