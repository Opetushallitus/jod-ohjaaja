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
import fi.okm.jod.ohjaaja.domain.JodUser;
import fi.okm.jod.ohjaaja.dto.ArtikkelinKommenttiDto;
import fi.okm.jod.ohjaaja.dto.SivuDto;
import fi.okm.jod.ohjaaja.entity.ArtikkelinKommentti;
import fi.okm.jod.ohjaaja.entity.FeatureFlag;
import fi.okm.jod.ohjaaja.repository.ArtikkelinKommentinIlmiantoRepository;
import fi.okm.jod.ohjaaja.repository.ArtikkelinKommenttiRepository;
import fi.okm.jod.ohjaaja.repository.OhjaajaRepository;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ArtikkelinKommenttiService {

  private final ArtikkelinKommenttiRepository artikkelinKommentit;
  private final ArtikkelinKommentinIlmiantoRepository artikkelinKommentinIlmianto;
  private final OhjaajaRepository ohjaajat;

  private static final Document.OutputSettings outputSettings =
      new Document.OutputSettings().prettyPrint(false);

  @FeatureRequired(FeatureFlag.Feature.COMMENTS)
  public ArtikkelinKommenttiDto add(
      @NotNull JodUser jodUser, long artikkeliId, @NotNull String kommentti) {
    var cleanedKommentti = clean(kommentti, "", Safelist.relaxed(), outputSettings);
    var ohjaaja = ohjaajat.getReferenceById(jodUser.getId());
    var artikkelinKommentti = new ArtikkelinKommentti(ohjaaja, artikkeliId, cleanedKommentti);
    return ArtikkelinKommenttiMapper.mapArtikkelinKommentti(
        artikkelinKommentit.save(artikkelinKommentti));
  }

  @FeatureRequired(FeatureFlag.Feature.COMMENTS)
  public void delete(@NotNull JodUser jodUser, UUID kommenttiId) {
    var ohjaaja = ohjaajat.getReferenceById(jodUser.getId());
    var artikkelinKommentti = artikkelinKommentit.getReferenceById(kommenttiId);
    if (artikkelinKommentti.getOhjaaja().getId().equals(ohjaaja.getId())) {
      artikkelinKommentit.delete(artikkelinKommentti);
    }
  }

  @FeatureRequired(FeatureFlag.Feature.COMMENTS)
  public SivuDto<ArtikkelinKommenttiDto> findByArtikkeliId(long artikkeliId, Pageable pageable) {
    var kommentit = artikkelinKommentit.findByArtikkeliId(artikkeliId, pageable);
    return new SivuDto<>(
        kommentit.map(ArtikkelinKommenttiMapper::mapArtikkelinKommentti).getContent(),
        kommentit.getTotalElements(),
        kommentit.getTotalPages());
  }

  @FeatureRequired(FeatureFlag.Feature.COMMENTS)
  public void ilmianna(UUID artikkelinKommenttiId, @Nullable JodUser jodUser) {
    boolean tunnistautunut = jodUser != null;
    artikkelinKommentinIlmianto.upsertIlmianto(artikkelinKommenttiId, tunnistautunut);
  }
}
