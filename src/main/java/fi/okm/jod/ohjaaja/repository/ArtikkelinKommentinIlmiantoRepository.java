/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.repository;

import fi.okm.jod.ohjaaja.entity.ArtikkelinKommentinIlmianto;
import fi.okm.jod.ohjaaja.entity.ArtikkelinKommentinIlmiantoId;
import fi.okm.jod.ohjaaja.repository.projection.IlmiantoYhteenveto;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ArtikkelinKommentinIlmiantoRepository
    extends JpaRepository<ArtikkelinKommentinIlmianto, ArtikkelinKommentinIlmiantoId> {
  @Modifying
  @Transactional
  @Query(
      value =
          """
          INSERT INTO artikkelin_kommentin_ilmianto (artikkelin_kommentti_id, tunnistautunut, maara, viimeksi_ilmiannettu)
          VALUES (:artikkelinKommenttiId, :tunnistautunut, 1, CURRENT_TIMESTAMP)
          ON CONFLICT (artikkelin_kommentti_id, tunnistautunut)
          DO UPDATE SET maara = artikkelin_kommentin_ilmianto.maara + 1, viimeksi_ilmiannettu = CURRENT_TIMESTAMP
          """,
      nativeQuery = true)
  void upsertIlmianto(UUID artikkelinKommenttiId, boolean tunnistautunut);

  List<ArtikkelinKommentinIlmianto> findByArtikkelinKommenttiId(UUID artikkelinKommenttiId);

  @Query(
      value =
          """
          SELECT
              k.id  AS artikkelinKommenttiId,
              k.kommentti AS kommentti,
              k.luotu AS kommentinAika,
              k.artikkeli_erc AS artikkeliErc,
              SUM(CASE WHEN a.tunnistautunut THEN a.maara ELSE 0 END) AS kirjautuneetMaara,
              SUM(CASE WHEN NOT a.tunnistautunut THEN a.maara ELSE 0 END) AS anonyymitMaara,
              MAX(a.viimeksi_ilmiannettu) AS viimeisinIlmianto
          FROM artikkelin_kommentti k
          JOIN artikkelin_kommentin_ilmianto a ON k.id = a.artikkelin_kommentti_id
          GROUP BY k.id
          ORDER BY MAX(a.viimeksi_ilmiannettu) DESC
          """,
      nativeQuery = true)
  List<IlmiantoYhteenveto> getAllIlmiantoYhteenveto();

  @Modifying
  @Transactional
  @Query(
      value =
          "DELETE FROM artikkelin_kommentin_ilmianto WHERE artikkelin_kommentti_id = :kommenttiId",
      nativeQuery = true)
  void deleteAllByArtikkelinKommenttiId(UUID kommenttiId);
}
