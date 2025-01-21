/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.repository;

import fi.okm.jod.ohjaaja.entity.Ohjaaja;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OhjaajaRepository extends JpaRepository<Ohjaaja, UUID> {

  @Query(value = "SELECT tunnistus.generate_ohjaaja_id(:henkiloId)", nativeQuery = true)
  UUID findIdByHenkiloId(String henkiloId);

  @Query(value = "SELECT tunnistus.remove_ohjaaja_id(:ohjaajaId)", nativeQuery = true)
  void removeId(UUID ohjaajaId);
}
