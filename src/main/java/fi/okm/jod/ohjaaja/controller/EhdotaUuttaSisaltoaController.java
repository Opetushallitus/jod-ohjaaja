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
import fi.okm.jod.ohjaaja.dto.SisaltoEhdotusDto;
import fi.okm.jod.ohjaaja.dto.validationgroup.Add;
import fi.okm.jod.ohjaaja.service.SisaltoEhdotusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sisaltoehdotus")
@RequiredArgsConstructor
@Tag(name = "sisaltoehdotus")
public class EhdotaUuttaSisaltoaController {

  private final SisaltoEhdotusService service;

  @PostMapping
  @Operation(summary = "Sends a content suggestion")
  void sendSisaltoEhdotus(
      @AuthenticationPrincipal JodUser user,
      @Validated(Add.class) @RequestBody SisaltoEhdotusDto sisaltoEhdotusDto) {
    service.sendSisaltoEhdotus(sisaltoEhdotusDto);
  }
}
