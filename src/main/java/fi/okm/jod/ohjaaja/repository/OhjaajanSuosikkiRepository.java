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
import fi.okm.jod.ohjaaja.entity.OhjaajanSuosikki;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OhjaajanSuosikkiRepository extends JpaRepository<OhjaajanSuosikki, UUID> {
  long deleteByOhjaajaAndId(Ohjaaja ohjaaja, UUID id);

  List<OhjaajanSuosikki> findByOhjaaja(Ohjaaja ohjaaja);

  Optional<OhjaajanSuosikki> findByOhjaajaAndArtikkeliId(Ohjaaja ohjaaja, Long artikkeliId);
}
