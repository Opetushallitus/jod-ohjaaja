/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service;

import static org.junit.jupiter.api.Assertions.*;

import fi.okm.jod.ohjaaja.repository.ArtikkelinKatseluRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;

@Import({ArtikkelinKatseluService.class})
class ArtikkelinKatseluServiceTest extends AbstractServiceTest {

  @Autowired private ArtikkelinKatseluService service;
  @Autowired private ArtikkelinKatseluRepository repository;

  @Test
  void shouldAddArtikkelinKatseluForAnoonymiId() {
    UUID ohjaajaId = null;
    var anonyymiId = "anonyymi123";
    var artikkeliId = 1L;
    var id = service.add(ohjaajaId, anonyymiId, artikkeliId);

    assertNotNull(id);
    var katselu = repository.findById(id).orElseThrow();
    assertEquals(ohjaajaId, katselu.getOhjaajaId());
    assertEquals(anonyymiId, katselu.getAnonyymiId());
    assertEquals(artikkeliId, katselu.getArtikkeliId());
    assertNotNull(katselu.getLuotu());
  }

  @Test
  void shouldAddArtikkelinKatseluForOhjaajaId() {
    UUID ohjaajaId = UUID.randomUUID();
    String anonyymiId = null;
    var artikkeliId = 1L;
    var id = service.add(ohjaajaId, anonyymiId, artikkeliId);

    assertNotNull(id);
    var katselu = repository.findById(id).orElseThrow();
    assertEquals(ohjaajaId, katselu.getOhjaajaId());
    assertEquals(anonyymiId, katselu.getAnonyymiId());
    assertEquals(artikkeliId, katselu.getArtikkeliId());
    assertNotNull(katselu.getLuotu());
  }

  @Test
  void shouldNotAddArtikkelinKatseluForBothOhjaajaIdAndAnonyymiId() {
    UUID ohjaajaId = UUID.randomUUID();
    String anonyymiId = "anonyymi123";
    var artikkeliId = 1L;

    assertThrows(
        ServiceValidationException.class, () -> service.add(ohjaajaId, anonyymiId, artikkeliId));
  }

  @Test
  void shouldNotAddArtikkelinKatseluForNeitherOhjaajaIdNorAnonyymiId() {
    UUID ohjaajaId = null;
    String anonyymiId = null;
    var artikkeliId = 1L;

    assertThrows(
        ServiceValidationException.class, () -> service.add(ohjaajaId, anonyymiId, artikkeliId));
  }

  @Test
  void shouldReturnEmptySetWhenNoArtikkelinKatseluExists() {
    var result = service.findMostRecentViewedArtikkeliIdsByUser(user, Pageable.ofSize(20));
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldRetrieveMostRecentViewedArtikkeliId() {

    var artikkeliId = 1L;
    service.add(user.getId(), null, artikkeliId);

    var result = service.findMostRecentViewedArtikkeliIdsByUser(user, Pageable.ofSize(20));

    assertFalse(result.isEmpty());
    assertEquals(1, result.getTotalElements());
    assertEquals(1L, result.getContent().getFirst());
  }

  @Test
  void shouldReturnMostRecentArtikkeliIdsInOrder() {
    var artikkeliId1 = 1L;
    var artikkeliId2 = 2L;
    var artikkeliId3 = 3L;

    service.add(user.getId(), null, artikkeliId1);
    service.add(user.getId(), null, artikkeliId2);
    service.add(user.getId(), null, artikkeliId3);
    service.add(user.getId(), null, artikkeliId2);

    var result = service.findMostRecentViewedArtikkeliIdsByUser(user, Pageable.ofSize(20));

    assertFalse(result.isEmpty());
    assertEquals(3, result.getTotalElements());
    assertEquals(artikkeliId2, result.getContent().get(0));
    assertEquals(artikkeliId3, result.getContent().get(1));
    assertEquals(artikkeliId1, result.getContent().get(2));
  }
}
