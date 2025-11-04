package com.sgivu.purchasesale.security;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Interceptor que copia el JWT del contexto de seguridad actual en la cabecera Authorization de las
 * llamadas que realiza el {@link org.springframework.web.client.RestClient} a otros microservicios.
 */
@Component
public class JwtAuthorizationInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String tokenValue = extractTokenValue(authentication);

    if (tokenValue != null && !tokenValue.isBlank()) {
      HttpHeaders headers = request.getHeaders();
      if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
        headers.setBearerAuth(tokenValue);
      }
    }

    return execution.execute(request, body);
  }

  @Nullable
  private String extractTokenValue(@Nullable Authentication authentication) {
    if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
      return jwtAuthenticationToken.getToken().getTokenValue();
    }
    return null;
  }
}

