/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.controller;

import fi.okm.jod.ohjaaja.dto.FeatureFlagDto;
import fi.okm.jod.ohjaaja.entity.FeatureFlag;
import fi.okm.jod.ohjaaja.service.FeatureFlagService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal-api/features")
@RequiredArgsConstructor
@Tag(name = "internal/features")
public class InternalFeatureToggleController {

  private final FeatureFlagService service;

  @PutMapping("/{feature}")
  public void setFeatureFlag(
      @PathVariable FeatureFlag.Feature feature, @RequestParam boolean enabled) {
    service.setFeatureFlag(feature, enabled);
  }

  @GetMapping("")
  public List<FeatureFlagDto> getFeatureFlags() {
    return service.list();
  }
}
