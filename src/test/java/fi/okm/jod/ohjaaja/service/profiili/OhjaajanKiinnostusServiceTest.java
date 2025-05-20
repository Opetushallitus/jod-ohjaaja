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

import fi.okm.jod.ohjaaja.dto.profiili.KiinnostusDto;
import fi.okm.jod.ohjaaja.service.AbstractServiceTest;
import fi.okm.jod.ohjaaja.service.NotFoundException;
import java.util.UUID;
import org.hibernate.AssertionFailure;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

@Import({OhjaajanKiinnostusService.class})
class OhjaajanKiinnostusServiceTest extends AbstractServiceTest {

  @Autowired private OhjaajanKiinnostusService service;

  @Test
  @WithMockUser
  void shoudAddOhjaajanKiinnostus() throws AssertionFailure {

    final long asiasanaId = 123L;
    var newId = service.add(user, asiasanaId);
    var kiinnostukset = service.findAll(user);

    assertEquals(1, kiinnostukset.size());
    var kiinnostus = kiinnostukset.getFirst();
    assertNotNull(kiinnostus.id());
    assertEquals(newId, kiinnostus.id());
    assertNotNull(kiinnostus.luotu());
    assertEquals(asiasanaId, kiinnostus.asiasanaId());
  }

  @Test
  @WithMockUser
  void shouldNotAddSameDataTwice() throws AssertionFailure {
    final long asiasanaId = 123L;
    var newId = service.add(user, asiasanaId);
    var newId2 = service.add(user, asiasanaId);
    assertEquals(newId, newId2);

    var kiinnostukset = service.findAll(user);
    assertEquals(1, kiinnostukset.size());
  }

  @Test
  @WithMockUser
  void shouldRemoveOhjaajanKiinnostus() throws AssertionFailure {
    final long asiasanaId1 = 1123L;
    final long asiasanaId2 = 2123L;
    final long asiasanaId3 = 3123L;
    var newId1 = service.add(user, asiasanaId1);
    var newId2 = service.add(user, asiasanaId2);
    var newId3 = service.add(user, asiasanaId3);
    var kiinnostuksetBeforeDelete = service.findAll(user);

    assertEquals(3, kiinnostuksetBeforeDelete.size());
    service.delete(user, newId2);
    var kiinnostuksetAfterDelete = service.findAll(user);
    assertEquals(2, kiinnostuksetAfterDelete.size());
    var ids = kiinnostuksetAfterDelete.stream().map(KiinnostusDto::id).toList();
    assertTrue(ids.contains(newId1));
    assertFalse(ids.contains(newId2));
    assertTrue(ids.contains(newId3));
  }

  @Test
  @WithMockUser
  void shouldThrowIfTriedToDeleteNonExistingKiinnostus() throws AssertionFailure {
    final UUID randomId = UUID.randomUUID();
    assertThrows(NotFoundException.class, () -> service.delete(user, randomId));
  }
}
