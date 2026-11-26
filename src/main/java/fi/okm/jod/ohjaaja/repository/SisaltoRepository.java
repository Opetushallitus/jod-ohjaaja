/*
 * Copyright (c) 2026 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.repository;

import fi.okm.jod.ohjaaja.entity.Sisalto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface SisaltoRepository extends JpaRepository<Sisalto, String> {

  /** Inserts the content type unless a row for the ERC already exists. */
  @Modifying
  @Transactional
  @Query(
      value =
          """
          INSERT INTO sisalto (erc, sisaltotyyppi, paivitetty)
          VALUES (:erc, :sisaltotyyppi, now())
          ON CONFLICT (erc) DO NOTHING
          """,
      nativeQuery = true)
  void insertIfAbsent(String erc, String sisaltotyyppi);
}
