/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service.profiili;

import static org.junit.jupiter.api.Assertions.*;

import fi.okm.jod.ohjaaja.dto.profiili.SuosikkiDto;
import fi.okm.jod.ohjaaja.service.AbstractServiceTest;
import fi.okm.jod.ohjaaja.service.NotFoundException;
import java.util.UUID;
import org.hibernate.AssertionFailure;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

@Import({OhjaajanSuosikkiService.class})
class OhjaajanSuosikkiServiceTest extends AbstractServiceTest {

  @Autowired private OhjaajanSuosikkiService service;

  @Test
  @WithMockUser
  void shoudAddOhjaajanSuosikki() throws AssertionFailure {

    final String artikkeliErc = "external-reference-code";
    var newId = service.add(user, artikkeliErc);
    var suosikit = service.findAll(user);

    assertEquals(1, suosikit.size());
    var suosikki = suosikit.getFirst();
    assertNotNull(suosikki.id());
    assertEquals(newId, suosikki.id());
    assertNotNull(suosikki.luotu());
    assertEquals(artikkeliErc, suosikki.artikkeliErc());
  }

  @Test
  @WithMockUser
  void shouldNotAddSameDataTwice() throws AssertionFailure {
    final String artikkeliErc = "external-reference-code";
    var newId = service.add(user, artikkeliErc);
    var newId2 = service.add(user, artikkeliErc);
    assertEquals(newId, newId2);

    var suosikit = service.findAll(user);
    assertEquals(1, suosikit.size());
  }

  @Test
  @WithMockUser
  void shouldRemoveOhjaajanSuosikki() throws AssertionFailure {
    final String artikkeliErc1 = "external-reference-code1";
    final String artikkeliErc2 = "external-reference-code2";
    final String artikkeliErc3 = "external-reference-code3";
    var newId1 = service.add(user, artikkeliErc1);
    var newId2 = service.add(user, artikkeliErc2);
    var newId3 = service.add(user, artikkeliErc3);
    var suosikitBeforeDelete = service.findAll(user);

    assertEquals(3, suosikitBeforeDelete.size());
    service.delete(user, newId2);
    var suosikitAfterDelete = service.findAll(user);
    assertEquals(2, suosikitAfterDelete.size());
    var ids = suosikitAfterDelete.stream().map(SuosikkiDto::id).toList();
    assertTrue(ids.contains(newId1));
    assertFalse(ids.contains(newId2));
    assertTrue(ids.contains(newId3));
  }

  @Test
  @WithMockUser
  void shouldThrowIfTriedToDeleteNonExistingSuosikki() throws AssertionFailure {
    final UUID randomId = UUID.randomUUID();
    assertThrows(NotFoundException.class, () -> service.delete(user, randomId));
  }
}
