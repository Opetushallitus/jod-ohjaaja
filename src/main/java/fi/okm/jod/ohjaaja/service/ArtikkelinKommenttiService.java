/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service;

import static org.jsoup.Jsoup.clean;

import fi.okm.jod.ohjaaja.annotation.FeatureRequired;
import fi.okm.jod.ohjaaja.config.logging.LogMarker;
import fi.okm.jod.ohjaaja.domain.JodUser;
import fi.okm.jod.ohjaaja.dto.ArtikkelinKommenttiDto;
import fi.okm.jod.ohjaaja.dto.KommentoidutArtikkelitDto;
import fi.okm.jod.ohjaaja.dto.PalauteViestiDto;
import fi.okm.jod.ohjaaja.dto.SivuDto;
import fi.okm.jod.ohjaaja.entity.ArtikkelinKommentti;
import fi.okm.jod.ohjaaja.entity.FeatureFlag;
import fi.okm.jod.ohjaaja.repository.ArtikkelinKommentinIlmiantoRepository;
import fi.okm.jod.ohjaaja.repository.ArtikkelinKommenttiRepository;
import fi.okm.jod.ohjaaja.repository.OhjaajaRepository;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class ArtikkelinKommenttiService {

  private final PalauteKanavaService palauteKanavaService;
  private final ArtikkelinKommenttiRepository artikkelinKommentit;
  private final ArtikkelinKommentinIlmiantoRepository artikkelinKommentinIlmianto;
  private final OhjaajaRepository ohjaajat;

  public ArtikkelinKommenttiService(
      @Autowired(required = false) PalauteKanavaService palauteKanavaService,
      ArtikkelinKommenttiRepository artikkelinKommentit,
      ArtikkelinKommentinIlmiantoRepository artikkelinKommentinIlmianto,
      OhjaajaRepository ohjaajat) {
    this.palauteKanavaService = palauteKanavaService;
    this.artikkelinKommentit = artikkelinKommentit;
    this.artikkelinKommentinIlmianto = artikkelinKommentinIlmianto;
    this.ohjaajat = ohjaajat;
  }

  private static final Document.OutputSettings outputSettings =
      new Document.OutputSettings().prettyPrint(false);

  /*
   * List all comments with pagination
   * Note! This method is not protected with @FeatureRequired
   * because it is used in admin interface to list all comments for moderation purposes.
   */
  public SivuDto<ArtikkelinKommenttiDto> findAll(Pageable pageable) {
    var kommentit = artikkelinKommentit.findAll(pageable);
    return new SivuDto<>(
        kommentit.map(ArtikkelinKommenttiMapper::mapArtikkelinKommentti).getContent(),
        kommentit.getTotalElements(),
        kommentit.getTotalPages());
  }

  @FeatureRequired(FeatureFlag.Feature.COMMENTS)
  public ArtikkelinKommenttiDto add(
      @NotNull JodUser jodUser, @NotNull String artikkeliErc, @NotNull String kommentti) {
    var cleanedKommentti =
        Parser.unescapeEntities(clean(kommentti, "", Safelist.relaxed(), outputSettings), false);
    var ohjaaja = ohjaajat.getReferenceById(jodUser.getId());
    var artikkelinKommentti =
        ArtikkelinKommenttiMapper.mapArtikkelinKommentti(
            artikkelinKommentit.save(
                new ArtikkelinKommentti(ohjaaja, artikkeliErc, cleanedKommentti)));
    log.atInfo()
        .addMarker(LogMarker.AUDIT)
        .addKeyValue("userId", ohjaaja.getId())
        .addKeyValue("commentId", artikkelinKommentti.id())
        .log("New comment added");
    return artikkelinKommentti;
  }

  @FeatureRequired(FeatureFlag.Feature.COMMENTS)
  public void delete(@NotNull JodUser jodUser, UUID kommenttiId) {
    var ohjaaja = ohjaajat.getReferenceById(jodUser.getId());
    var artikkelinKommentti = artikkelinKommentit.getReferenceById(kommenttiId);
    if (artikkelinKommentti.getOhjaaja().getId().equals(ohjaaja.getId())) {
      artikkelinKommentit.delete(artikkelinKommentti);
      log.atInfo()
          .addMarker(LogMarker.AUDIT)
          .addKeyValue("userId", ohjaaja.getId())
          .addKeyValue("commentId", artikkelinKommentti)
          .log("Comment deleted");
    }
  }

  @FeatureRequired(FeatureFlag.Feature.COMMENTS)
  public SivuDto<ArtikkelinKommenttiDto> findByArtikkeliErc(
      String artikkeliErc, Pageable pageable) {
    var kommentit = artikkelinKommentit.findByArtikkeliErc(artikkeliErc, pageable);
    return new SivuDto<>(
        kommentit.map(ArtikkelinKommenttiMapper::mapArtikkelinKommentti).getContent(),
        kommentit.getTotalElements(),
        kommentit.getTotalPages());
  }

  @FeatureRequired(FeatureFlag.Feature.COMMENTS)
  public SivuDto<KommentoidutArtikkelitDto> findKommentoidutArtikkelit(
      @NotNull JodUser jodUser, Pageable pageable) {
    var ohjaaja = ohjaajat.getReferenceById(jodUser.getId());
    var queryResults = artikkelinKommentit.findKommentoidutArtikkelitByOhjaaja(ohjaaja, pageable);
    var mappedResults =
        queryResults.map(ArtikkelinKommenttiMapper::mapKommentoidutArtikkelit).toList();
    return new SivuDto<>(
        mappedResults, queryResults.getTotalElements(), queryResults.getTotalPages());
  }

  @FeatureRequired(FeatureFlag.Feature.COMMENTS)
  public void ilmianna(UUID artikkelinKommenttiId, @Nullable JodUser jodUser) {
    boolean tunnistautunut = jodUser != null;
    var ilmiannotKpl =
        artikkelinKommentinIlmianto.upsertIlmianto(artikkelinKommenttiId, tunnistautunut);
    if (ilmiannotKpl == 1) {
      var ilmiantoIlmoitusViesti =
          new PalauteViestiDto(
              "Asiaton kommentti",
              "Ohjaajan osio",
              "Ohjaajan osio",
              null,
              "fi",
              "Artikkelin kommentti on saanut ensimmäisen ilmiannon. \n"
                  + "Tarkista kommentti virkailijan käyttöliittymästä ja tarvittaessa poista se.",
              Instant.now());

      if (palauteKanavaService != null) {
        palauteKanavaService.sendMessage(ilmiantoIlmoitusViesti);
      } else {
        throw new IllegalStateException("PalauteKanavaService is not available");
      }
    }
  }
}
