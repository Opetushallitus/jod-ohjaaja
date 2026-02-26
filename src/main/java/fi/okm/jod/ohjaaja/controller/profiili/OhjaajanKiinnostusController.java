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
import fi.okm.jod.ohjaaja.dto.profiili.KiinnostusDto;
import fi.okm.jod.ohjaaja.dto.validationgroup.Add;
import fi.okm.jod.ohjaaja.service.profiili.OhjaajanKiinnostusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiili/kiinnostukset")
@RequiredArgsConstructor
@Tag(name = "profiili/kiinnostukset")
public class OhjaajanKiinnostusController {
  private final OhjaajanKiinnostusService service;

  @GetMapping
  @Operation(summary = "Finds all Ohjaaja's kiinnostukset")
  List<KiinnostusDto> findAll(@AuthenticationPrincipal JodUser user) {
    return service.findAll(user);
  }

  @PostMapping
  @Operation(summary = "Add a new Ohjaaja's kiinnostus")
  UUID add(
      @AuthenticationPrincipal JodUser user, @Validated(Add.class) @RequestBody KiinnostusDto dto) {
    return service.add(user, dto.asiasanaId());
  }

  @DeleteMapping
  @Operation(summary = "Delete an Ohjaaja's kiinnostus")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@AuthenticationPrincipal JodUser user, @RequestParam UUID id) {
    service.delete(user, id);
  }
}
