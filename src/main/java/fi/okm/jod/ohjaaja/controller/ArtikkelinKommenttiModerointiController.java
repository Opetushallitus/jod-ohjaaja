/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.controller;

import fi.okm.jod.ohjaaja.repository.projection.IlmiantoYhteenveto;
import fi.okm.jod.ohjaaja.service.ArtikkelinKommenttiModerointiService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal-api/moderointi/kommentit")
@RequiredArgsConstructor
@Tag(name = "internal/moderointi/kommentit")
public class ArtikkelinKommenttiModerointiController {
  private final ArtikkelinKommenttiModerointiService service;

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
