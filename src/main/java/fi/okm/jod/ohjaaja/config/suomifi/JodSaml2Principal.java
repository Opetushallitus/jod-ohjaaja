/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.config.suomifi;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fi.okm.jod.ohjaaja.domain.JodUser;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.util.CollectionUtils;

@SuppressWarnings("java:S4544")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonIgnoreProperties(ignoreUnknown = true)
class JodSaml2Principal implements AuthenticatedPrincipal, JodUser {

  private final UUID id;
  @Getter private final Map<String, List<Object>> attributes;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  JodSaml2Principal(
      @JsonProperty("attributes") Map<String, List<Object>> attributes,
      @JsonProperty("id") UUID id) {
    this.id = requireNonNull(id);
    this.attributes = requireNonNull(attributes);
  }

  @Override
  @JsonIgnore
  public String getName() {
    return id.toString();
  }

  @Override
  public UUID getId() {
    return id;
  }

  @JsonIgnore
  @Override
  public String givenName() {
    return getAttribute(Attribute.GIVEN_NAME)
        .or(() -> getAttribute(Attribute.FIRST_NAME))
        .orElse(null);
  }

  @JsonIgnore
  @Override
  public String familyName() {
    return getAttribute(Attribute.SN).or(() -> getAttribute(Attribute.FAMILY_NAME)).orElse(null);
  }

  @JsonIgnore
  public Optional<String> getAttribute(Attribute attribute) {
    return Optional.ofNullable(getFirstAttribute(attribute.getUri()));
  }

  @Nullable
  @SuppressWarnings("unchecked")
  private <A> A getFirstAttribute(String name) {
    List<A> values = (List<A>) attributes.get(name);
    return CollectionUtils.firstElement(values);
  }
}
