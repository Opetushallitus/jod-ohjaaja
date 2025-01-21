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
import fi.okm.jod.ohjaaja.service.profiili.OhjaajaService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    return new OhjaajaCsrfDto(
        user.givenName(),
        user.familyName(),
        new CsrfTokenDto(
            csrfToken.getToken(), csrfToken.getHeaderName(), csrfToken.getParameterName()));
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(HttpServletRequest request, @AuthenticationPrincipal JodUser user)
      throws ServletException {
    ohjaajaService.deleteProfiili(user);
    request.logout();
  }
}
