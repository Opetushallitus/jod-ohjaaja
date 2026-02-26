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
import fi.okm.jod.ohjaaja.dto.CsrfTokenDto;
import fi.okm.jod.ohjaaja.dto.profiili.OhjaajaCsrfDto;
import fi.okm.jod.ohjaaja.dto.profiili.OhjaajaDto;
import fi.okm.jod.ohjaaja.dto.profiili.export.OhjaajaExportDto;
import fi.okm.jod.ohjaaja.service.profiili.OhjaajaService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiili/ohjaaja")
@Tag(name = "profiili/ohjaaja")
@RequiredArgsConstructor
public class OhjaajaController {
  private final OhjaajaService ohjaajaService;

  @GetMapping
  public OhjaajaCsrfDto get(
      @AuthenticationPrincipal JodUser user, @Parameter(hidden = true) CsrfToken csrfToken) {
    var ohjaaja = ohjaajaService.get(user);
    return new OhjaajaCsrfDto(
        ohjaaja.id(),
        user.givenName(),
        user.familyName(),
        new CsrfTokenDto(
            csrfToken.getToken(), csrfToken.getHeaderName(), csrfToken.getParameterName()),
        ohjaaja.tyoskentelyPaikka());
  }

  @PutMapping
  public void update(@AuthenticationPrincipal JodUser user, @RequestBody @Valid OhjaajaDto dto) {
    ohjaajaService.update(user, dto);
  }

  @GetMapping("/vienti")
  public ResponseEntity<OhjaajaExportDto> export(@AuthenticationPrincipal JodUser user) {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=profiili.json")
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .body(ohjaajaService.export(user));
  }
}
