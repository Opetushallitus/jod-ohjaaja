/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service;

import fi.okm.jod.ohjaaja.repository.ArtikkelinKommentinIlmiantoRepository;
import fi.okm.jod.ohjaaja.repository.ArtikkelinKommenttiRepository;
import fi.okm.jod.ohjaaja.repository.projection.IlmiantoYhteenveto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ArtikkelinKommenttiModerointiService {
  private final ArtikkelinKommentinIlmiantoRepository artikkelinKommentinIlmiantoRepository;
  private final ArtikkelinKommenttiRepository artikkelinKommenttiRepository;

  public List<IlmiantoYhteenveto> getAllIlmiantoYhteenveto() {
    return artikkelinKommentinIlmiantoRepository.getAllIlmiantoYhteenveto();
  }

  public void deleteArtikkelinKommentti(UUID artikkelinKommenttiId) {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    artikkelinKommenttiRepository.deleteById(artikkelinKommenttiId);
    log.info("User {} deleted comment {}", userId, artikkelinKommenttiId);
  }

  public void deleteIlmiannot(UUID artikkelinKommenttiId) {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    artikkelinKommentinIlmiantoRepository.deleteAllByArtikkelinKommenttiId(artikkelinKommenttiId);
    log.info("User {} deleted reports for comment {}", userId, artikkelinKommenttiId);
  }
}
