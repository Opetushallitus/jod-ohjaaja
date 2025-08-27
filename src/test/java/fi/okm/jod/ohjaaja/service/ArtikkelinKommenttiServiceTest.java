/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service;

import static org.junit.jupiter.api.Assertions.*;

import fi.okm.jod.ohjaaja.entity.ArtikkelinKommentinIlmianto;
import fi.okm.jod.ohjaaja.entity.Ohjaaja;
import fi.okm.jod.ohjaaja.repository.ArtikkelinKommentinIlmiantoRepository;
import fi.okm.jod.ohjaaja.testutil.TestJodUser;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;

@Import({ArtikkelinKommenttiService.class})
class ArtikkelinKommenttiServiceTest extends AbstractServiceTest {
  @Autowired private ArtikkelinKommenttiService service;
  @Autowired private ArtikkelinKommentinIlmiantoRepository ilmiantoRepository;

  @Test
  @WithMockUser
  void addCommentCleansHtmlContent() {
    var commentWithHtml = "<script>alert('XSS');</script>Valid comment";
    var result = service.add(user, "external-reference-code", commentWithHtml);
    assertNotNull(result);
    assertEquals("Valid comment", result.kommentti());
  }

  @Test
  @WithMockUser
  void addCommentWithValidContent() {
    var artikkeliErc = "external-reference-code";
    var commentContent = "Valid comment";
    var result = service.add(user, artikkeliErc, commentContent);
    assertNotNull(result);
    assertEquals(commentContent, result.kommentti());
    assertEquals(artikkeliErc, result.artikkeliErc());
  }

  @Test
  @WithMockUser
  void addCommentWithEmptyContent() {
    var artikkeliErc = "external-reference-code";
    service.add(user, artikkeliErc, "");
    var result = service.findByArtikkeliErc(artikkeliErc, Pageable.ofSize(10));
    assertNotNull(result);
    assertEquals(1, result.maara());
    assertEquals("", result.sisalto().getFirst().kommentti());
    assertEquals(artikkeliErc, result.sisalto().getFirst().artikkeliErc());
  }

  @Test
  @WithMockUser
  void addCommentWithNullContentThrowsException() {
    var artikkeliErc = "external-reference-code";
    assertThrows(NullPointerException.class, () -> service.add(user, artikkeliErc, null));
  }

  @Test
  @WithMockUser
  void addCommentWithHTMLContentWorks() {
    var artikkeliErc = "external-reference-code";
    var commentContent = "<b>Bold comment</b>";
    var result = service.add(user, artikkeliErc, commentContent);
    assertNotNull(result);
    assertEquals(commentContent, result.kommentti());
    assertEquals(artikkeliErc, result.artikkeliErc());
  }

  @Test
  void findByArtikkeliErcReturnsPaginatedResults() {
    var pageable = Pageable.ofSize(10);
    var artikkeliErc = "external-reference-code";
    var commentId = service.add(user, artikkeliErc, "Test comment 1").id();
    var result = service.findByArtikkeliErc(artikkeliErc, pageable);
    assertNotNull(result);
    assertEquals(1, result.maara());
    assertEquals("Test comment 1", result.sisalto().getFirst().kommentti());
    assertEquals(commentId, result.sisalto().getFirst().id());
  }

  @Test
  void addCommentThrowsExceptionForNullUser() {
    assertThrows(
        NullPointerException.class,
        () -> service.add(null, "external-reference-code", "Valid comment"));
  }

  @Test
  @WithMockUser
  void deleteCommentByOwnerSucceeds() {
    var artikkeliErc = "external-reference-code";
    var testComment1Id = service.add(user, artikkeliErc, "Test comment 1").id();
    var testComment2Id = service.add(user, artikkeliErc, "Test comment 2").id();

    service.delete(user, testComment1Id);

    var comments = service.findByArtikkeliErc(artikkeliErc, Pageable.ofSize(10));
    assertNotNull(comments);
    assertEquals(1, comments.maara());
    assertEquals("Test comment 2", comments.sisalto().getFirst().kommentti());
    assertEquals(testComment2Id, comments.sisalto().getFirst().id());
  }

  @Test
  @WithMockUser
  void cantDeleteCommentByNonOwner() {
    var artikkeliErc = "external-reference-code";
    var otherUser = new TestJodUser(entityManager.persist(new Ohjaaja(UUID.randomUUID())).getId());
    var testComment1Id = service.add(otherUser, artikkeliErc, "Test comment 1").id();
    var testComment2Id = service.add(user, artikkeliErc, "Test comment 2").id();

    service.delete(user, testComment1Id);

    var comments = service.findByArtikkeliErc(artikkeliErc, Pageable.ofSize(10));
    assertNotNull(comments);
    assertEquals(2, comments.maara());
    assertEquals("Test comment 1", comments.sisalto().get(0).kommentti());
    assertEquals("Test comment 2", comments.sisalto().get(1).kommentti());
    assertEquals(testComment1Id, comments.sisalto().get(0).id());
    assertEquals(testComment2Id, comments.sisalto().get(1).id());
  }

  @Test
  void anonymousUserCanAddArtikkelinKommenttiIlmianto() {
    var artikkeliErc = "external-reference-code";
    var commentContent = "Ilmianto kommentti";
    var result = service.add(user, artikkeliErc, commentContent);
    service.ilmianna(result.id(), null);
    var ilmiannot = ilmiantoRepository.findByArtikkelinKommenttiId(result.id());
    assertNotNull(ilmiannot);
    assertEquals(1, ilmiannot.size());
    assertEquals(1, ilmiannot.getFirst().getMaara());
    assertFalse(ilmiannot.getFirst().isTunnistautunut());
  }

  @Test
  void anonymousUserCanAddMultipleIlmiannot() {
    var artikkeliErc = "external-reference-code";
    var commentContent = "Ilmianto kommentti";
    var result = service.add(user, artikkeliErc, commentContent);
    service.ilmianna(result.id(), null);
    service.ilmianna(result.id(), null);
    var ilmiannot = ilmiantoRepository.findByArtikkelinKommenttiId(result.id());
    assertNotNull(ilmiannot);
    assertEquals(1, ilmiannot.size());
    assertEquals(2, ilmiannot.getFirst().getMaara());
    assertFalse(ilmiannot.getFirst().isTunnistautunut());
  }

  @Test
  @WithMockUser
  void authenticatedUserCanAddArtikkelinKommenttiIlmianto() {
    var artikkeliErc = "external-reference-code";
    var commentContent = "Ilmianto kommentti";
    var result = service.add(user, artikkeliErc, commentContent);
    service.ilmianna(result.id(), user);
    var ilmiannot = ilmiantoRepository.findByArtikkelinKommenttiId(result.id());
    assertNotNull(ilmiannot);
    assertEquals(1, ilmiannot.size());
    assertEquals(1, ilmiannot.getFirst().getMaara());
    assertTrue(ilmiannot.getFirst().isTunnistautunut());
  }

  @Test
  @WithMockUser
  void authenticatedUserCanAddMultipleIlmiannot() {
    var artikkeliErc = "external-reference-code";
    var commentContent = "Ilmianto kommentti";
    var result = service.add(user, artikkeliErc, commentContent);
    service.ilmianna(result.id(), user);
    service.ilmianna(result.id(), user);
    var ilmiannot = ilmiantoRepository.findByArtikkelinKommenttiId(result.id());
    assertNotNull(ilmiannot);
    assertEquals(1, ilmiannot.size());
    assertEquals(2, ilmiannot.getFirst().getMaara());
    assertTrue(ilmiannot.getFirst().isTunnistautunut());
  }

  @Test
  @WithMockUser
  void authenticatedAndUnautheticatedIlminantoCreatesTwoRows() {
    var artikkeliErc = "external-reference-code";
    var commentContent = "Ilmianto kommentti";
    var result = service.add(user, artikkeliErc, commentContent);

    // Unauthenticated ilmianto
    service.ilmianna(result.id(), null);

    // Authenticated ilmianto
    service.ilmianna(result.id(), user);

    var ilmiannot = ilmiantoRepository.findByArtikkelinKommenttiId(result.id());
    assertNotNull(ilmiannot);
    assertEquals(2, ilmiannot.size());

    // Check unauthenticated ilmianto
    var unauthenticatedIlmianto =
        ilmiannot.stream().filter(ilmianto -> !ilmianto.isTunnistautunut()).findFirst();
    assertTrue(unauthenticatedIlmianto.isPresent());
    assertEquals(1, unauthenticatedIlmianto.get().getMaara());

    // Check authenticated ilmianto
    var authenticatedIlmianto =
        ilmiannot.stream().filter(ArtikkelinKommentinIlmianto::isTunnistautunut).findFirst();
    assertTrue(authenticatedIlmianto.isPresent());
    assertEquals(1, authenticatedIlmianto.get().getMaara());
  }

  @Test
  @WithMockUser
  void authenticatedUserCantAddIlmiantoToNonExistentComment() {
    var nonExistentCommentId = UUID.randomUUID();
    assertThrows(
        DataIntegrityViolationException.class, () -> service.ilmianna(nonExistentCommentId, user));
  }
}
