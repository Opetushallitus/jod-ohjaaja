/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.errorhandler;

import fi.okm.jod.ohjaaja.errorhandler.ErrorInfo.ErrorCode;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Fallback error controller for errors that are not handled elsewhere. */
@RestController
@Hidden
@Slf4j
public class FallbackErrorController implements ErrorController {

  private final @Nullable Tracer tracer;

  public FallbackErrorController(ObjectProvider<Tracer> tracer) {
    this.tracer = tracer.getIfAvailable();
  }

  /** Renders (almost all) unhandled errors as JSON. */
  @SuppressWarnings({"java:S6857", "java:S3752"})
  @RequestMapping(path = "${server.error.path:/error}")
  public ResponseEntity<ErrorInfo> error(HttpServletRequest request) {

    HttpStatus status;
    Throwable exception;
    ErrorCode errorCode = ErrorCode.INVALID_REQUEST;

    if (request.getAttribute(WebAttributes.ACCESS_DENIED_403) instanceof Throwable ex) {
      exception = ex;
      status = HttpStatus.FORBIDDEN;
      errorCode = ErrorCode.ACCESS_DENIED;
    } else {
      exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
      if (exception instanceof AuthenticationException) {
        status = HttpStatus.FORBIDDEN;
        errorCode = ErrorCode.AUTHENTICATION_FAILURE;
      } else {
        status = getStatus(request);
      }
    }

    if (status.is4xxClientError()
        && request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI) instanceof String uri
        && (uri.startsWith("/logout") || uri.startsWith("/login"))) {
      // we need to redirect login/logout failures back to the frontend application
      log.info(
          "Login or logout failure {}: {}",
          status.value(),
          request.getAttribute(RequestDispatcher.ERROR_MESSAGE));

      return ResponseEntity.status(HttpStatus.SEE_OTHER)
          .location(URI.create("/?error=AUTHENTICATION_FAILURE"))
          .body(
              new ErrorInfo(
                  ErrorCode.AUTHENTICATION_FAILURE, traceId(tracer), List.of(status.name())));
    }

    if (status.is5xxServerError()) {
      errorCode = ErrorCode.UNSPECIFIED_ERROR;
      log.atError()
          .addKeyValue("status", status.value())
          .log("Request failed due to server error", exception);
    } else {
      log.atWarn()
          .addKeyValue("status", status.value())
          .addKeyValue("reason", exception == null ? null : exception.toString())
          .log("Request failed due to client error");
    }

    return ResponseEntity.status(status)
        .header("Cache-Control", "private, no-cache, no-store, stale-if-error=0")
        .header("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'")
        .body(new ErrorInfo(errorCode, traceId(tracer), List.of(status.name())));
  }

  private static @Nullable String traceId(@Nullable Tracer tracer) {
    return (tracer != null && tracer.currentSpan() instanceof Span span)
        ? span.context().traceId()
        : null;
  }

  private HttpStatus getStatus(HttpServletRequest request) {
    Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    if (statusCode == null) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    try {
      return HttpStatus.valueOf(statusCode);
    } catch (Exception ex) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }
}
