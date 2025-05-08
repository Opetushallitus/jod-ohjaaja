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
import fi.okm.jod.ohjaaja.repository.ViimeksiKatseltuArtikkeliRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;

@Import({ArtikkelinKatseluService.class})
class ArtikkelinKatseluServiceTest extends AbstractServiceTest {

  @Autowired private ArtikkelinKatseluService service;
  @Autowired private ArtikkelinKatseluRepository artikkelinKatseluRepository;
  @Autowired private ViimeksiKatseltuArtikkeliRepository viimeksiKatseltuArtikkeliRepository;

  @Test
  void shouldAddOnlyArtikkelinKatseluWhenUserIsNull() {
    var artikkeliId = 1L;
    service.add(null, artikkeliId);

    var artikkelinKatselut = artikkelinKatseluRepository.findAll();
    assertEquals(1, artikkelinKatselut.size());
    var katselu = artikkelinKatselut.getFirst();
    assertEquals(1L, katselu.getArtikkeliId());
    assertEquals(1, katselu.getMaara());
    assertEquals(LocalDate.now(), katselu.getPaiva());

    var viimeksiKatsellut = viimeksiKatseltuArtikkeliRepository.findAll();
    assertEquals(0, viimeksiKatsellut.size());
  }

  @Test
  void shouldAddArtikkelinKatseluAndViimeksiKatseltuWhenUserIsLoggedIn() {
    var artikkeliId = 1L;
    service.add(user, artikkeliId);

    var artikkelinKatselut = artikkelinKatseluRepository.findAll();
    assertEquals(1, artikkelinKatselut.size());
    var katselu = artikkelinKatselut.getFirst();
    assertEquals(1L, katselu.getArtikkeliId());
    assertEquals(1, katselu.getMaara());
    assertEquals(LocalDate.now(), katselu.getPaiva());

    var viimeksiKatsellut =
        viimeksiKatseltuArtikkeliRepository.findByArtikkeliIdAndOhjaajaId(
            artikkeliId, user.getId());
    assertTrue(viimeksiKatsellut.isPresent());
    assertEquals(artikkeliId, viimeksiKatsellut.get().getArtikkeliId());
    assertEquals(user.getId(), viimeksiKatsellut.get().getOhjaajaId());
    assertNotNull(viimeksiKatsellut.get().getViimeksiKatseltu());
  }

  @Test
  void shouldIncreaseMaaraWhenArtikkeliIdAlreadyExists() {
    var artikkeliId = 1L;
    service.add(user, artikkeliId);
    service.add(user, artikkeliId);

    var artikkelinKatselut = artikkelinKatseluRepository.findAll();
    assertEquals(1, artikkelinKatselut.size());
    var katselu = artikkelinKatselut.getFirst();
    assertEquals(artikkeliId, katselu.getArtikkeliId());
    assertEquals(2, katselu.getMaara());
  }

  @Test
  void shouldNotAddArtikkelinKatseluWhenArtikkeliIdIsNull() {
    assertThrows(IllegalArgumentException.class, () -> service.add(null, null));
  }

  @Test
  void shouldReturnEmptySetWhenNoArtikkelinKatseluExists() {
    var result = service.findMostRecentViewedArtikkeliIdsByUser(user, Pageable.ofSize(20));
    assertEquals(0, result.maara());
  }

  @Test
  void shouldRetrieveMostRecentViewedArtikkeliId() {

    var artikkeliId = 1L;
    service.add(user, artikkeliId);

    var result = service.findMostRecentViewedArtikkeliIdsByUser(user, Pageable.ofSize(20));
    assertEquals(1, result.maara());
    assertEquals(1L, result.sisalto().getFirst());
  }

  @Test
  void shouldReturnMostRecentArtikkeliIdsInOrder() {
    var artikkeliId1 = 1L;
    var artikkeliId2 = 2L;
    var artikkeliId3 = 3L;

    service.add(user, artikkeliId1);
    service.add(user, artikkeliId2);
    service.add(user, artikkeliId3);
    service.add(user, artikkeliId2);

    var result = service.findMostRecentViewedArtikkeliIdsByUser(user, Pageable.ofSize(20));

    assertEquals(3, result.maara());
    assertEquals(artikkeliId2, result.sisalto().getFirst());
    assertEquals(artikkeliId3, result.sisalto().get(1));
    assertEquals(artikkeliId1, result.sisalto().get(2));
  }
}
