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

import fi.okm.jod.ohjaaja.entity.ArtikkelinKommentti;
import fi.okm.jod.ohjaaja.entity.Ohjaaja;
import fi.okm.jod.ohjaaja.repository.ArtikkelinKommentinIlmiantoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({ArtikkelinKommenttiModerointiService.class})
class ArtikkelinKommenttiModerointiServiceTest extends AbstractServiceTest {
  @Autowired private ArtikkelinKommenttiModerointiService service;
  @Autowired private ArtikkelinKommentinIlmiantoRepository ilmiantoRepository;

  @Test
  void shouldReturnEmptyListWhenNoComments() {
    var comments = service.getAllIlmiantoYhteenveto();
    assertTrue(comments.isEmpty(), "Expected no comments to be returned");
  }

  @Test
  void shouldReturnEmptyListWhenNoIlmianto() {

    var ohjaaja = entityManager.find(Ohjaaja.class, user.getId());
    entityManager.persist(new ArtikkelinKommentti(ohjaaja, 1L, "Test Comment"));

    var ilmianto = service.getAllIlmiantoYhteenveto();
    assertTrue(ilmianto.isEmpty(), "Expected no ilmianto to be returned");
  }

  @Test
  void shouldReturnOneIlmiantoYhteenvetoWhenThereIsOnlyOneAnonymousIlmianto() {

    var ohjaaja = entityManager.find(Ohjaaja.class, user.getId());
    var kommentti = entityManager.persist(new ArtikkelinKommentti(ohjaaja, 1L, "Test Comment"));

    ilmiantoRepository.upsertIlmianto(kommentti.getId(), false);

    var ilmiannot = service.getAllIlmiantoYhteenveto();
    assertEquals(1, ilmiannot.size(), "Expected one ilmianto to be returned");
    assertEquals(
        kommentti.getId(),
        ilmiannot.getFirst().getArtikkelinKommenttiId(),
        "Expected ilmianto to match the comment ID");
    assertEquals(
        1,
        ilmiannot.getFirst().getAnonyymitMaara(),
        "Expected ilmianto to have one anonymous report");
    assertEquals(
        0,
        ilmiannot.getFirst().getKirjautuneetMaara(),
        "Expected ilmianto to have no logged-in reports");
  }

  @Test
  void shouldReturnOneIlmiantoYhteenvetoWhenThereIsOneAnonymousAndOneAuthenticatedIlmianto() {

    var ohjaaja = entityManager.find(Ohjaaja.class, user.getId());
    var kommentti = entityManager.persist(new ArtikkelinKommentti(ohjaaja, 1L, "Test Comment"));

    ilmiantoRepository.upsertIlmianto(kommentti.getId(), false);
    ilmiantoRepository.upsertIlmianto(kommentti.getId(), true);

    var ilmiannot = service.getAllIlmiantoYhteenveto();
    assertEquals(1, ilmiannot.size(), "Expected one ilmianto to be returned");
    assertEquals(
        kommentti.getId(),
        ilmiannot.getFirst().getArtikkelinKommenttiId(),
        "Expected ilmianto to match the comment ID");
    assertEquals(
        1,
        ilmiannot.getFirst().getAnonyymitMaara(),
        "Expected ilmianto to have one anonymous report");
    assertEquals(
        1,
        ilmiannot.getFirst().getKirjautuneetMaara(),
        "Expected ilmianto to have no logged-in reports");
  }
}
