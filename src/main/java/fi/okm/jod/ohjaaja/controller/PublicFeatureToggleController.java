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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feature")
@RequiredArgsConstructor
@Tag(name = "feature")
public class PublicFeatureToggleController {

  private final FeatureFlagService service;

  @GetMapping("/{feature}")
  public FeatureFlagDto isFeatureEnabled(@PathVariable FeatureFlag.Feature feature) {
    return new FeatureFlagDto(feature, service.isFeatureEnabled(feature));
  }
}
