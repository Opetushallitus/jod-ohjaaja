/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.aspect;

import fi.okm.jod.ohjaaja.annotation.FeatureRequired;
import fi.okm.jod.ohjaaja.service.FeatureFlagService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
@RequiredArgsConstructor
public class FeatureCheckAspect {

  private final FeatureFlagService featureFlagService;

  @Before("@annotation(featureRequired)")
  public void checkFeatureEnabled(FeatureRequired featureRequired) {
    if (!featureFlagService.isFeatureEnabled(featureRequired.value())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }
}
