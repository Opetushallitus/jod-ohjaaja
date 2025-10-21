/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service.profiili;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fi.okm.jod.ohjaaja.IntegrationTest;
import fi.okm.jod.ohjaaja.domain.JodUser;
import fi.okm.jod.ohjaaja.entity.Ohjaaja;
import fi.okm.jod.ohjaaja.repository.OhjaajaRepository;
import fi.okm.jod.ohjaaja.testutil.TestJodUser;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ProfileUpdatedAspectTest extends IntegrationTest {
  @Autowired private OhjaajaService service;
  @Autowired private OhjaajaRepository ohjaajat;
  private MockAppender mockAppender;

  private JodUser jodUser;

  @BeforeEach
  void before() {
    var id = ohjaajat.findIdByHenkiloId("TEST:" + UUID.randomUUID());
    var user = new Ohjaaja(id);
    ohjaajat.save(user);
    jodUser = new TestJodUser(id);

    Logger logger = (Logger) LoggerFactory.getLogger(ProfileUpdatedAspect.class);
    mockAppender = new MockAppender();
    logger.addAppender(mockAppender);
    mockAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
    mockAppender.start();
  }

  @AfterEach
  void after() {
    if (mockAppender != null) {
      mockAppender.stop();
      ((Logger) LoggerFactory.getLogger(ProfileUpdatedAspect.class)).detachAppender(mockAppender);
    }
  }

  @Test
  void shouldLogProfileUpdate() {
    service.get(jodUser);
    assertFalse(mockAppender.anyMatch("profile updated"));

    service.delete(jodUser);
    assertTrue(mockAppender.anyMatch("profile updated"));
  }

  static class MockAppender extends ListAppender<ILoggingEvent> {
    public boolean anyMatch(String message) {
      return list.stream()
          .map(ILoggingEvent::getFormattedMessage)
          .anyMatch(msg -> msg.contains(message));
    }
  }
}
