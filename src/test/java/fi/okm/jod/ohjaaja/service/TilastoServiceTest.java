/*
 * Copyright (c) 2026 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fi.okm.jod.ohjaaja.dto.ArtikkeliMaaraDto;
import fi.okm.jod.ohjaaja.dto.KiinnostusMaaraDto;
import fi.okm.jod.ohjaaja.dto.TyoskentelyPaikkaMaaraDto;
import fi.okm.jod.ohjaaja.entity.ArtikkelinKommentti;
import fi.okm.jod.ohjaaja.entity.Ohjaaja;
import fi.okm.jod.ohjaaja.entity.OhjaajanKiinnostus;
import fi.okm.jod.ohjaaja.entity.OhjaajanSuosikki;
import fi.okm.jod.ohjaaja.entity.TyoskentelyPaikka;
import fi.okm.jod.ohjaaja.repository.ArtikkelinKatseluRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({TilastoService.class})
class TilastoServiceTest extends AbstractServiceTest {

  private static final LocalDate PAIVA = LocalDate.of(2026, 3, 15);

  @Autowired private TilastoService service;
  @Autowired private ArtikkelinKatseluRepository katseluRepository;

  @Test
  void shouldReturnEmptyStatisticsWithOnlyOneUser() {
    var tilastot = service.getTilastot(null, null, 10);

    assertEquals(1, tilastot.kayttajienMaara());
    assertEquals(TyoskentelyPaikka.values().length + 1, tilastot.tyoskentelyPaikat().size());
    assertEquals(new TyoskentelyPaikkaMaaraDto(null, 1), tilastot.tyoskentelyPaikat().getLast());
    assertEquals(0, tilastot.kiinnostukset().size());
    assertEquals(0, tilastot.suosituimmatArtikkelit().size());
    assertEquals(0, tilastot.kommentoiduimmatArtikkelit().size());
    assertEquals(0, tilastot.katsotuimmatArtikkelit().size());
  }

  @Test
  void shouldReturnUserCountAndWorkplaceDistribution() {
    createOhjaaja(TyoskentelyPaikka.PERUSASTE);
    createOhjaaja(TyoskentelyPaikka.PERUSASTE);
    createOhjaaja(TyoskentelyPaikka.KORKEAKOULU);

    var tilastot = service.getTilastot(null, null, 10);

    assertEquals(4, tilastot.kayttajienMaara());
    var paikat = tilastot.tyoskentelyPaikat();
    assertEquals(
        new TyoskentelyPaikkaMaaraDto(TyoskentelyPaikka.PERUSASTE, 2),
        find(paikat, TyoskentelyPaikka.PERUSASTE));
    assertEquals(
        new TyoskentelyPaikkaMaaraDto(TyoskentelyPaikka.KORKEAKOULU, 1),
        find(paikat, TyoskentelyPaikka.KORKEAKOULU));
    assertEquals(
        new TyoskentelyPaikkaMaaraDto(TyoskentelyPaikka.MUU, 0),
        find(paikat, TyoskentelyPaikka.MUU));
    assertEquals(new TyoskentelyPaikkaMaaraDto(null, 1), find(paikat, null));
    assertEquals(4, paikat.stream().mapToLong(TyoskentelyPaikkaMaaraDto::maara).sum());
  }

  @Test
  void shouldReturnInterestDistributionInDescendingOrder() {
    var ohjaaja1 = entityManager.find(Ohjaaja.class, user.getId());
    var ohjaaja2 = createOhjaaja(null);
    entityManager.persist(new OhjaajanKiinnostus(1L, ohjaaja1));
    entityManager.persist(new OhjaajanKiinnostus(2L, ohjaaja1));
    entityManager.persist(new OhjaajanKiinnostus(2L, ohjaaja2));

    var tilastot = service.getTilastot(null, null, 10);

    assertEquals(
        List.of(new KiinnostusMaaraDto(2L, 2), new KiinnostusMaaraDto(1L, 1)),
        tilastot.kiinnostukset());
  }

  @Test
  void shouldReturnTopArticlesInDescendingOrderLimitedByTop() {
    var ohjaaja1 = entityManager.find(Ohjaaja.class, user.getId());
    var ohjaaja2 = createOhjaaja(null);
    var aika = Instant.now();
    createSuosikki(ohjaaja1, "a", aika);
    createSuosikki(ohjaaja1, "b", aika);
    createSuosikki(ohjaaja2, "b", aika);
    createSuosikki(ohjaaja1, "c", aika);
    createKommentti(ohjaaja1, "c", aika);
    createKommentti(ohjaaja2, "c", aika);
    createKommentti(null, "c", aika);
    createKommentti(ohjaaja1, "a", aika);
    katseluRepository.upsertKatselu("a", PAIVA);
    katseluRepository.upsertKatselu("a", PAIVA.plusDays(1));
    katseluRepository.upsertKatselu("a", PAIVA.plusDays(1));
    katseluRepository.upsertKatselu("b", PAIVA);

    var tilastot = service.getTilastot(null, null, 2);

    assertEquals(
        List.of(new ArtikkeliMaaraDto("b", 2), new ArtikkeliMaaraDto("a", 1)),
        tilastot.suosituimmatArtikkelit());
    assertEquals(
        List.of(new ArtikkeliMaaraDto("c", 3), new ArtikkeliMaaraDto("a", 1)),
        tilastot.kommentoiduimmatArtikkelit());
    assertEquals(
        List.of(new ArtikkeliMaaraDto("a", 3), new ArtikkeliMaaraDto("b", 1)),
        tilastot.katsotuimmatArtikkelit());
  }

  @Test
  void shouldFilterTopArticlesByTimeRange() {
    var ohjaaja = entityManager.find(Ohjaaja.class, user.getId());
    var zone = ZoneId.systemDefault();
    var ennen = PAIVA.minusDays(1).atTime(23, 59).atZone(zone).toInstant();
    var alussa = PAIVA.atStartOfDay(zone).toInstant();
    var lopussa = PAIVA.plusDays(1).atTime(23, 59).atZone(zone).toInstant();
    var jalkeen = PAIVA.plusDays(2).atStartOfDay(zone).toInstant();

    createSuosikki(ohjaaja, "ennen", ennen);
    createSuosikki(ohjaaja, "alussa", alussa);
    createSuosikki(ohjaaja, "lopussa", lopussa);
    createSuosikki(ohjaaja, "jalkeen", jalkeen);
    createKommentti(ohjaaja, "ennen", ennen);
    createKommentti(ohjaaja, "alussa", alussa);
    createKommentti(ohjaaja, "lopussa", lopussa);
    createKommentti(ohjaaja, "jalkeen", jalkeen);
    katseluRepository.upsertKatselu("ennen", PAIVA.minusDays(1));
    katseluRepository.upsertKatselu("alussa", PAIVA);
    katseluRepository.upsertKatselu("lopussa", PAIVA.plusDays(1));
    katseluRepository.upsertKatselu("jalkeen", PAIVA.plusDays(2));

    var tilastot = service.getTilastot(PAIVA, PAIVA.plusDays(1), 10);

    var odotettu = List.of(new ArtikkeliMaaraDto("alussa", 1), new ArtikkeliMaaraDto("lopussa", 1));
    assertEquals(odotettu, tilastot.suosituimmatArtikkelit());
    assertEquals(odotettu, tilastot.kommentoiduimmatArtikkelit());
    assertEquals(odotettu, tilastot.katsotuimmatArtikkelit());

    var vainAlku = service.getTilastot(PAIVA.plusDays(1), null, 10);
    assertEquals(
        List.of(new ArtikkeliMaaraDto("jalkeen", 1), new ArtikkeliMaaraDto("lopussa", 1)),
        vainAlku.katsotuimmatArtikkelit());

    var vainLoppu = service.getTilastot(null, PAIVA, 10);
    assertEquals(
        List.of(new ArtikkeliMaaraDto("alussa", 1), new ArtikkeliMaaraDto("ennen", 1)),
        vainLoppu.suosituimmatArtikkelit());
  }

  @Test
  void shouldFailWhenAlkuIsAfterLoppu() {
    var nextDay = PAIVA.plusDays(1);
    assertThrows(ServiceValidationException.class, () -> service.getTilastot(nextDay, PAIVA, 10));
  }

  private Ohjaaja createOhjaaja(TyoskentelyPaikka tyoskentelyPaikka) {
    var ohjaaja = new Ohjaaja(ohjaajaRepository.findIdByHenkiloId("TEST:" + UUID.randomUUID()));
    ohjaaja.setTyoskentelyPaikka(tyoskentelyPaikka);
    return entityManager.persist(ohjaaja);
  }

  private void createSuosikki(Ohjaaja ohjaaja, String artikkeliErc, Instant luotu) {
    entityManager.persist(new OhjaajanSuosikki(null, luotu, ohjaaja, artikkeliErc));
  }

  private void createKommentti(Ohjaaja ohjaaja, String artikkeliErc, Instant luotu) {
    entityManager.persist(
        new ArtikkelinKommentti(null, ohjaaja, artikkeliErc, luotu, "kommentti", null));
  }

  private static TyoskentelyPaikkaMaaraDto find(
      List<TyoskentelyPaikkaMaaraDto> paikat, TyoskentelyPaikka paikka) {
    return paikat.stream().filter(p -> p.tyoskentelyPaikka() == paikka).findFirst().orElseThrow();
  }
}
