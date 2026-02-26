/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Ticker;
import fi.okm.jod.ohjaaja.dto.FeatureFlagDto;
import fi.okm.jod.ohjaaja.entity.FeatureFlag;
import fi.okm.jod.ohjaaja.repository.FeatureFlagRepository;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@DependsOnDatabaseInitialization
@Slf4j
public class FeatureFlagService {

  private final FeatureFlagRepository featureFlags;
  private final LoadingCache<FeatureFlag.Feature, Boolean> featureCache;
  static final Duration CACHE_DURATION = Duration.ofMinutes(1);

  @Autowired
  FeatureFlagService(FeatureFlagRepository featureFlagsRepository) {
    this(featureFlagsRepository, Ticker.systemTicker(), ForkJoinPool.commonPool());
  }

  FeatureFlagService(
      FeatureFlagRepository featureFlagsRepository, Ticker ticker, Executor executor) {
    this.featureFlags = featureFlagsRepository;
    this.featureCache =
        Caffeine.newBuilder()
            .refreshAfterWrite(CACHE_DURATION)
            .initialCapacity(FeatureFlag.Feature.values().length)
            .ticker(ticker)
            .executor(executor)
            .build(new Loader(featureFlags));
  }

  public boolean isFeatureEnabled(FeatureFlag.Feature feature) {
    return Boolean.TRUE.equals(featureCache.get(feature));
  }

  @PreAuthorize(
      "hasAuthority('SCOPE_' + @environment.getProperty('jod.ohjaaja.internal-api.oauth2-scope'))")
  public void setFeatureFlag(FeatureFlag.Feature feature, boolean enabled) {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    var updatedBy = authentication.getName();
    var flag = featureFlags.findById(feature).orElse(new FeatureFlag(feature));
    flag.setEnabled(enabled);
    flag.setUpdatedBy(updatedBy);
    featureFlags.save(flag);
    featureCache.put(feature, enabled);
    log.info("Feature flag {} updated to {}", feature, enabled);
  }

  public List<FeatureFlagDto> list() {
    return featureFlags.findAll().stream()
        .map(flag -> new FeatureFlagDto(flag.getFeature(), flag.isEnabled()))
        .toList();
  }

  @RequiredArgsConstructor
  private static class Loader implements CacheLoader<FeatureFlag.Feature, Boolean> {
    private final FeatureFlagRepository featureFlagRepository;

    @Override
    public Boolean load(FeatureFlag.Feature key) {
      return reload(key, null);
    }

    @Override
    public Boolean reload(FeatureFlag.Feature key, Boolean oldValue) {
      return featureFlagRepository.findById(key).map(FeatureFlag::isEnabled).orElse(false);
    }
  }
}
