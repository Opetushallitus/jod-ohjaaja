/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service.profiili;

import static org.assertj.core.api.Assertions.assertThat;

import fi.okm.jod.ohjaaja.dto.profiili.export.OhjaajaExportDto;
import fi.okm.jod.ohjaaja.entity.Ohjaaja;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class ExportMapperTest {
  @Test
  void testNoNewOrDeletedMappingMethods() {
    Set<String> expectedMethods =
        Set.of(
            "mapOhjaaja", "mapOhjaajanSuosikki", "mapOhjaajanKiinnostus", "mapArtikkelinKommentti");

    Set<String> actualMethods =
        Set.of(ExportMapper.class.getDeclaredMethods()).stream()
            .filter(
                method ->
                    Modifier.isPublic(method.getModifiers())
                        && Modifier.isStatic(method.getModifiers()))
            .map(Method::getName)
            .collect(Collectors.toSet());

    assertThat(actualMethods).containsExactlyInAnyOrderElementsOf(expectedMethods);
  }

  @Test
  void testOhjaajaMappingCompleteness() {
    assertMappingCompleteness(Ohjaaja.class, OhjaajaExportDto.class, Set.of());
  }

  private void assertMappingCompleteness(Class<?> entityClass, Class<?> dtoClass) {
    assertMappingCompleteness(entityClass, dtoClass, Set.of());
  }

  private void assertMappingCompleteness(
      Class<?> entityClass, Class<?> dtoClass, Set<String> ignoredGetters) {
    Set<String> entityGetters = getGetterNames(entityClass, ignoredGetters);
    Set<String> dtoGetters = getGetterNames(dtoClass, Set.of());
    assertThat(entityGetters).isSubsetOf(dtoGetters);
  }

  private Set<String> getGetterNames(Class<?> clazz, Set<String> ignoredGetters) {
    if (clazz.isRecord()) {
      return Set.of(clazz.getRecordComponents()).stream()
          .map(RecordComponent::getName)
          .filter(
              componentName -> !ignoredGetters.contains(componentName)) // Skip ignored components
          .collect(Collectors.toSet());
    } else {
      return Set.of(clazz.getDeclaredMethods()).stream()
          .filter(method -> Modifier.isPublic(method.getModifiers())) // Only public methods
          .filter(
              method ->
                  method.getName().startsWith("get")
                      || method.getName().startsWith("is")) // Getter methods
          .map(method -> method.getName().replaceFirst("^(get|is)", "")) // Remove prefix
          .map(
              name ->
                  Character.toLowerCase(name.charAt(0))
                      + name.substring(1)) // Convert to field name
          .filter(getterName -> !ignoredGetters.contains(getterName)) // Skip ignored getters
          .collect(Collectors.toSet());
    }
  }
}
