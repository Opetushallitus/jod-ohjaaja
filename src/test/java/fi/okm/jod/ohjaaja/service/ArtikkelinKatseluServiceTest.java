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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.okm.jod.ohjaaja.entity.SisaltoTyyppi;
import fi.okm.jod.ohjaaja.repository.ArtikkelinKatseluRepository;
import fi.okm.jod.ohjaaja.repository.SisaltoRepository;
import fi.okm.jod.ohjaaja.repository.ViimeksiKatseltuArtikkeliRepository;
import fi.okm.jod.ohjaaja.service.cms.CmsClient;
import fi.okm.jod.ohjaaja.service.cms.CmsException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import({ArtikkelinKatseluService.class})
class ArtikkelinKatseluServiceTest extends AbstractServiceTest {

  @Autowired private ArtikkelinKatseluService service;
  @Autowired private ArtikkelinKatseluRepository artikkelinKatseluRepository;
  @Autowired private ViimeksiKatseltuArtikkeliRepository viimeksiKatseltuArtikkeliRepository;
  @Autowired private SisaltoRepository sisaltoRepository;
  @MockitoBean private CmsClient cmsClient;

  @BeforeEach
  void setUpCms() {
    when(cmsClient.getSisaltoTyyppi(anyString())).thenReturn(Optional.of(SisaltoTyyppi.ARTIKKELI));
  }

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
    var result = service.findMostRecentViewedArtikkeliErcsByUser(user, null, Pageable.ofSize(20));
    assertEquals(0, result.maara());
  }

  @Test
  void shouldRetrieveMostRecentViewedartikkeliErc() {

    final var artikkeliErc = "external-reference-code";
    service.add(user, artikkeliErc);

    var result = service.findMostRecentViewedArtikkeliErcsByUser(user, null, Pageable.ofSize(20));
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

    var result = service.findMostRecentViewedArtikkeliErcsByUser(user, null, Pageable.ofSize(20));

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

    var result = service.findMostViewedArtikkeliErcs(null, null, pageable);

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
        service.findMostViewedArtikkeliErcs(List.of(artikkeliErc1, artikkeliErc2), null, pageable);

    assertEquals(2, result.maara());
    assertEquals(artikkeliErc2, result.sisalto().getFirst());
    assertEquals(artikkeliErc1, result.sisalto().get(1));
  }

  @Test
  void shouldReturnEmptyListOfMostViewedArtikkeliWhenThereIsNoViews() {

    var pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "summa"));

    var result = service.findMostViewedArtikkeliErcs(null, null, pageable);

    assertEquals(0, result.maara());
    assertTrue(result.sisalto().isEmpty());
  }

  @Test
  void shouldStoreSisaltoTyyppiFromCms() {
    when(cmsClient.getSisaltoTyyppi("uutinen")).thenReturn(Optional.of(SisaltoTyyppi.UUTINEN));

    service.add(null, "uutinen");
    service.add(null, "artikkeli");

    assertEquals(
        SisaltoTyyppi.UUTINEN,
        sisaltoRepository.findById("uutinen").orElseThrow().getSisaltotyyppi());
    assertEquals(
        SisaltoTyyppi.ARTIKKELI,
        sisaltoRepository.findById("artikkeli").orElseThrow().getSisaltotyyppi());
    assertEquals(2, artikkelinKatseluRepository.count());
  }

  @Test
  void shouldFetchSisaltoTyyppiFromCmsOnlyOnce() {
    service.add(null, "artikkeli");
    service.add(user, "artikkeli");

    verify(cmsClient).getSisaltoTyyppi("artikkeli");
    assertEquals(2, artikkelinKatseluRepository.findAll().getFirst().getMaara());
  }

  @Test
  void shouldAddKatseluWithoutSisaltoWhenCmsFails() {
    when(cmsClient.getSisaltoTyyppi("artikkeli")).thenThrow(new CmsException("timeout"));

    service.add(user, "artikkeli");

    assertEquals(1, artikkelinKatseluRepository.count());
    assertEquals(1, viimeksiKatseltuArtikkeliRepository.count());
    assertTrue(sisaltoRepository.findById("artikkeli").isEmpty());

    // resolved again on the next view
    doReturn(Optional.of(SisaltoTyyppi.UUTINEN)).when(cmsClient).getSisaltoTyyppi("artikkeli");
    service.add(user, "artikkeli");
    assertEquals(
        SisaltoTyyppi.UUTINEN,
        sisaltoRepository.findById("artikkeli").orElseThrow().getSisaltotyyppi());
  }

  @Test
  void shouldNotAddKatseluWhenSisaltoIsNotFoundInCms() {
    when(cmsClient.getSisaltoTyyppi("tuntematon")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.add(user, "tuntematon"));

    assertEquals(0, artikkelinKatseluRepository.count());
    assertEquals(0, viimeksiKatseltuArtikkeliRepository.count());
    assertEquals(0, sisaltoRepository.count());
  }

  @Test
  void shouldFilterMostViewedBySisaltoTyyppi() {
    when(cmsClient.getSisaltoTyyppi("uutinen")).thenReturn(Optional.of(SisaltoTyyppi.UUTINEN));
    when(cmsClient.getSisaltoTyyppi("tuntematon-tyyppi")).thenThrow(new CmsException("error"));
    service.add(null, "artikkeli");
    service.add(null, "uutinen");
    service.add(null, "uutinen");
    service.add(null, "tuntematon-tyyppi");
    var pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "summa"));

    var oletus = service.findMostViewedArtikkeliErcs(null, null, pageable);
    assertEquals(Set.of("artikkeli", "tuntematon-tyyppi"), Set.copyOf(oletus.sisalto()));

    var uutiset =
        service.findMostViewedArtikkeliErcs(null, Set.of(SisaltoTyyppi.UUTINEN), pageable);
    assertEquals(List.of("uutinen"), uutiset.sisalto());

    var molemmat =
        service.findMostViewedArtikkeliErcs(
            null, Set.of(SisaltoTyyppi.ARTIKKELI, SisaltoTyyppi.UUTINEN), pageable);
    assertEquals(3, molemmat.maara());
    assertEquals("uutinen", molemmat.sisalto().getFirst());
  }

  @Test
  void shouldFilterMostRecentViewedBySisaltoTyyppi() {
    when(cmsClient.getSisaltoTyyppi("uutinen")).thenReturn(Optional.of(SisaltoTyyppi.UUTINEN));
    service.add(user, "artikkeli1");
    service.add(user, "uutinen");
    service.add(user, "artikkeli2");
    var pageable = Pageable.ofSize(20);

    assertEquals(
        List.of("artikkeli2", "artikkeli1"),
        service.findMostRecentViewedArtikkeliErcsByUser(user, null, pageable).sisalto());
    assertEquals(
        List.of("uutinen"),
        service
            .findMostRecentViewedArtikkeliErcsByUser(user, Set.of(SisaltoTyyppi.UUTINEN), pageable)
            .sisalto());
    assertEquals(
        List.of("artikkeli2", "uutinen", "artikkeli1"),
        service
            .findMostRecentViewedArtikkeliErcsByUser(
                user, Set.of(SisaltoTyyppi.ARTIKKELI, SisaltoTyyppi.UUTINEN), pageable)
            .sisalto());
  }

  @Test
  void shouldNotCallCmsWhenSisaltoTyyppiIsKnown() {
    sisaltoRepository.insertIfAbsent("artikkeli", SisaltoTyyppi.ARTIKKELI.name());

    service.add(null, "artikkeli");

    verify(cmsClient, never()).getSisaltoTyyppi(anyString());
  }
}
