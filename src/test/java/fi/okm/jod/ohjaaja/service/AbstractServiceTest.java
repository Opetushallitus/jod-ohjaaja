/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service;

import fi.okm.jod.ohjaaja.entity.Ohjaaja;
import fi.okm.jod.ohjaaja.repository.OhjaajaRepository;
import fi.okm.jod.ohjaaja.testutil.TestJodUser;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public abstract class AbstractServiceTest {

  @Autowired protected OhjaajaRepository ohjaajaRepository;

  @Container @ServiceConnection
  static PostgreSQLContainer postgreSQLContainer =
      new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
          .withEnv("LANG", "en_US.UTF-8")
          .withEnv("LC_ALL", "en_US.UTF-8");

  @Autowired protected TestEntityManager entityManager;
  protected TestJodUser user;

  @BeforeEach
  public void setUpUser() {
    var ohjaaja = new Ohjaaja(ohjaajaRepository.findIdByHenkiloId("TEST:" + UUID.randomUUID()));
    this.user = new TestJodUser(entityManager.persist(ohjaaja).getId());
  }

  /** Simulates commit by flushing and clearing the entity manager. */
  @AfterEach
  public void simulateCommit() {
    entityManager.flush();
    entityManager.clear();
  }
}
