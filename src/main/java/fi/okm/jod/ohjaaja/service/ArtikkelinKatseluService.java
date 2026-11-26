/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service;

import fi.okm.jod.ohjaaja.domain.JodUser;
import fi.okm.jod.ohjaaja.dto.SivuDto;
import fi.okm.jod.ohjaaja.entity.SisaltoTyyppi;
import fi.okm.jod.ohjaaja.entity.ViimeksiKatseltuArtikkeli;
import fi.okm.jod.ohjaaja.repository.ArtikkelinKatseluRepository;
import fi.okm.jod.ohjaaja.repository.OhjaajaRepository;
import fi.okm.jod.ohjaaja.repository.SisaltoRepository;
import fi.okm.jod.ohjaaja.repository.ViimeksiKatseltuArtikkeliRepository;
import fi.okm.jod.ohjaaja.repository.projection.SummaPerArtikkeli;
import fi.okm.jod.ohjaaja.service.cms.CmsClient;
import fi.okm.jod.ohjaaja.service.cms.CmsException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
public class ArtikkelinKatseluService {
  private final ArtikkelinKatseluRepository artikkelinKatselut;
  private final ViimeksiKatseltuArtikkeliRepository viimeksiKatseltuArtikkeli;
  private final OhjaajaRepository ohjaajat;
  private final SisaltoRepository sisallot;
  private final CmsClient cmsClient;
  private final TransactionTemplate transactionTemplate;

  public ArtikkelinKatseluService(
      ArtikkelinKatseluRepository artikkelinKatselut,
      ViimeksiKatseltuArtikkeliRepository viimeksiKatseltuArtikkeli,
      OhjaajaRepository ohjaajat,
      SisaltoRepository sisallot,
      CmsClient cmsClient,
      PlatformTransactionManager transactionManager) {
    this.artikkelinKatselut = artikkelinKatselut;
    this.viimeksiKatseltuArtikkeli = viimeksiKatseltuArtikkeli;
    this.ohjaajat = ohjaajat;
    this.sisallot = sisallot;
    this.cmsClient = cmsClient;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
  }

  /**
   * Records a view of the content.
   *
   * <p>If the type of the content is not yet known, it is fetched from the CMS (outside the
   * database transaction). Content unknown to the CMS is not recorded. If the CMS cannot be
   * queried, the view is recorded anyway and the type is resolved on a later view.
   *
   * @throws NotFoundException if the CMS does not have content with the ERC
   */
  public void add(@Nullable JodUser jodUser, String artikkeliErc) {
    if (artikkeliErc == null) {
      throw new IllegalArgumentException("artikkeliErc must not be null");
    }
    var tyyppi = resolveSisaltoTyyppi(artikkeliErc);

    transactionTemplate.executeWithoutResult(
        _ -> {
          if (tyyppi != null) {
            sisallot.insertIfAbsent(artikkeliErc, tyyppi.name());
          }
          addKatselu(jodUser, artikkeliErc);
        });
  }

  /** Returns the type to store, or null if it is already stored or cannot be resolved now. */
  @Nullable
  private SisaltoTyyppi resolveSisaltoTyyppi(String erc) {
    if (sisallot.existsById(erc)) {
      return null;
    }
    try {
      return cmsClient
          .getSisaltoTyyppi(erc)
          .orElseThrow(() -> new NotFoundException("Content not found"));
    } catch (CmsException e) {
      log.warn("Could not resolve content type of {} from CMS: {}", erc, e.getMessage());
      return null;
    }
  }

  private void addKatselu(@Nullable JodUser jodUser, String artikkeliErc) {
    artikkelinKatselut.upsertKatselu(artikkeliErc, LocalDate.now());
    if (jodUser != null) {
      viimeksiKatseltuArtikkeli.upsert(artikkeliErc, jodUser.getId(), Instant.now());
    }
  }

  @Transactional(readOnly = true)
  public SivuDto<String> findMostRecentViewedArtikkeliErcsByUser(
      JodUser user, @Nullable Collection<SisaltoTyyppi> tyypit, Pageable pageable) {
    var ohjaajaId = ohjaajat.getReferenceById(user.getId()).getId();
    return new SivuDto<>(
        viimeksiKatseltuArtikkeli
            .findByOhjaajaIdAndTyypit(ohjaajaId, orDefault(tyypit), pageable)
            .map(ViimeksiKatseltuArtikkeli::getArtikkeliErc));
  }

  @Transactional(readOnly = true)
  public SivuDto<String> findMostViewedArtikkeliErcs(
      @Nullable Collection<String> filterArtikkeliErcs,
      @Nullable Collection<SisaltoTyyppi> tyypit,
      Pageable pageable) {
    return new SivuDto<>(
        artikkelinKatselut
            .findSumKatselut(filterArtikkeliErcs, orDefault(tyypit), pageable)
            .map(SummaPerArtikkeli::getArtikkeliErc));
  }

  /** Only articles are returned by default. */
  private static Collection<SisaltoTyyppi> orDefault(@Nullable Collection<SisaltoTyyppi> tyypit) {
    return tyypit == null || tyypit.isEmpty() ? Set.of(SisaltoTyyppi.ARTIKKELI) : tyypit;
  }
}
