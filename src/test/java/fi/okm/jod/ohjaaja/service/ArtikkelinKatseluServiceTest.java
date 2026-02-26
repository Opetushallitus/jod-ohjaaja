/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fi.okm.jod.ohjaaja.repository.ArtikkelinKatseluRepository;
import fi.okm.jod.ohjaaja.repository.ViimeksiKatseltuArtikkeliRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Import({ArtikkelinKatseluService.class})
class ArtikkelinKatseluServiceTest extends AbstractServiceTest {

  @Autowired private ArtikkelinKatseluService service;
  @Autowired private ArtikkelinKatseluRepository artikkelinKatseluRepository;
  @Autowired private ViimeksiKatseltuArtikkeliRepository viimeksiKatseltuArtikkeliRepository;

  @Test
  void shouldAddOnlyArtikkelinKatseluWhenUserIsNull() {
    final var artikkeliErc = "external-reference-code";
    service.add(null, artikkeliErc);

    var artikkelinKatselut = artikkelinKatseluRepository.findAll();
    assertEquals(1, artikkelinKatselut.size());
    var katselu = artikkelinKatselut.getFirst();
    assertEquals(artikkeliErc, katselu.getArtikkeliErc());
    assertEquals(1, katselu.getMaara());
    assertEquals(LocalDate.now(), katselu.getPaiva());

    var viimeksiKatsellut = viimeksiKatseltuArtikkeliRepository.findAll();
    assertEquals(0, viimeksiKatsellut.size());
  }

  @Test
  void shouldAddArtikkelinKatseluAndViimeksiKatseltuWhenUserIsLoggedIn() {
    final var artikkeliErc = "external-reference-code";
    service.add(user, artikkeliErc);

    var artikkelinKatselut = artikkelinKatseluRepository.findAll();
    assertEquals(1, artikkelinKatselut.size());
    var katselu = artikkelinKatselut.getFirst();
    assertEquals(artikkeliErc, katselu.getArtikkeliErc());
    assertEquals(1, katselu.getMaara());
    assertEquals(LocalDate.now(), katselu.getPaiva());

    var viimeksiKatsellut =
        viimeksiKatseltuArtikkeliRepository.findByArtikkeliErcAndOhjaajaId(
            artikkeliErc, user.getId());
    assertTrue(viimeksiKatsellut.isPresent());
    assertEquals(artikkeliErc, viimeksiKatsellut.get().getArtikkeliErc());
    assertEquals(user.getId(), viimeksiKatsellut.get().getOhjaajaId());
    assertNotNull(viimeksiKatsellut.get().getViimeksiKatseltu());
  }

  @Test
  void shouldIncreaseMaaraWhenartikkeliErcAlreadyExists() {
    final var artikkeliErc = "external-reference-code";
    service.add(user, artikkeliErc);
    service.add(user, artikkeliErc);

    var artikkelinKatselut = artikkelinKatseluRepository.findAll();
    assertEquals(1, artikkelinKatselut.size());
    var katselu = artikkelinKatselut.getFirst();
    assertEquals(artikkeliErc, katselu.getArtikkeliErc());
    assertEquals(2, katselu.getMaara());
  }

  @Test
  void shouldNotAddArtikkelinKatseluWhenartikkeliErcIsNull() {
    assertThrows(IllegalArgumentException.class, () -> service.add(null, null));
  }

  @Test
  void shouldReturnEmptySetWhenNoArtikkelinKatseluExists() {
    var result = service.findMostRecentViewedArtikkeliErcsByUser(user, Pageable.ofSize(20));
    assertEquals(0, result.maara());
  }

  @Test
  void shouldRetrieveMostRecentViewedartikkeliErc() {

    final var artikkeliErc = "external-reference-code";
    service.add(user, artikkeliErc);

    var result = service.findMostRecentViewedArtikkeliErcsByUser(user, Pageable.ofSize(20));
    assertEquals(1, result.maara());
    assertEquals(artikkeliErc, result.sisalto().getFirst());
  }

  @Test
  void shouldReturnMostRecentartikkeliErcsInOrder() {
    final var artikkeliErc1 = "external-reference-code1";
    final var artikkeliErc2 = "external-reference-code2";
    final var artikkeliErc3 = "external-reference-code3";

    service.add(user, artikkeliErc1);
    service.add(user, artikkeliErc2);
    service.add(user, artikkeliErc3);
    service.add(user, artikkeliErc2);

    var result = service.findMostRecentViewedArtikkeliErcsByUser(user, Pageable.ofSize(20));

    assertEquals(3, result.maara());
    assertEquals(artikkeliErc2, result.sisalto().getFirst());
    assertEquals(artikkeliErc3, result.sisalto().get(1));
    assertEquals(artikkeliErc1, result.sisalto().get(2));
  }

  @Test
  void shouldReturnMostViewedartikkeliErcs() {
    final var artikkeliErc1 = "external-reference-code1";
    final var artikkeliErc2 = "external-reference-code2";
    final var artikkeliErc3 = "external-reference-code3";

    service.add(user, artikkeliErc1);
    service.add(user, artikkeliErc2);
    service.add(user, artikkeliErc3);
    service.add(user, artikkeliErc2);
    service.add(user, artikkeliErc2);
    service.add(user, artikkeliErc1);

    var pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "summa"));

    var result = service.findMostViewedArtikkeliErcs(null, pageable);

    assertEquals(3, result.maara());
    assertEquals(artikkeliErc2, result.sisalto().getFirst());
    assertEquals(artikkeliErc1, result.sisalto().get(1));
    assertEquals(artikkeliErc3, result.sisalto().get(2));
  }

  @Test
  void shouldReturnMostViewedartikkeliErcsFilteredByIds() {
    final var artikkeliErc1 = "external-reference-code1";
    final var artikkeliErc2 = "external-reference-code2";
    final var artikkeliErc3 = "external-reference-code3";

    service.add(null, artikkeliErc1);
    service.add(null, artikkeliErc2);
    service.add(null, artikkeliErc3);
    service.add(null, artikkeliErc2);

    var pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "summa"));

    var result =
        service.findMostViewedArtikkeliErcs(List.of(artikkeliErc1, artikkeliErc2), pageable);

    assertEquals(2, result.maara());
    assertEquals(artikkeliErc2, result.sisalto().getFirst());
    assertEquals(artikkeliErc1, result.sisalto().get(1));
  }

  @Test
  void shouldReturnEmptyListOfMostViewedArtikkeliWhenThereIsNoViews() {

    var pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "summa"));

    var result = service.findMostViewedArtikkeliErcs(null, pageable);

    assertEquals(0, result.maara());
    assertTrue(result.sisalto().isEmpty());
  }
}
