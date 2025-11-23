package com.sgivu.purchasesale.config;

import com.sgivu.purchasesale.client.ClientServiceClient;
import com.sgivu.purchasesale.client.UserServiceClient;
import com.sgivu.purchasesale.client.VehicleServiceClient;
import com.sgivu.purchasesale.security.JwtAuthorizationInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Configura los clientes HTTP declarativos que conectan con los microservicios SGIVU (clientes,
 * usuarios e inventario de vehículos). Propaga el JWT actual y la clave interna para autorizar
 * llamadas de servicio a servicio respetando la política de seguridad de la plataforma.
 */
@Configuration
public class AppConfig {

  @Value("${service.internal.secret-key}")
  private String internalServiceKey;

  private final ServicesProperties servicesProperties;

  public AppConfig(ServicesProperties servicesProperties) {
    this.servicesProperties = servicesProperties;
  }

  /**
   * Builder común para RestClient con load balancer y propagación de JWT. Permite que las llamadas
   * salientes honren la identidad del usuario autenticado y pasen por el Discovery Client de Spring
   * Cloud.
   */
  @Bean
  @LoadBalanced
  RestClient.Builder restClientBuilder(JwtAuthorizationInterceptor jwtAuthorizationInterceptor) {
    return RestClient.builder().requestInterceptor(jwtAuthorizationInterceptor);
  }

  /**
   * Proxy hacia el microservicio de clientes. Incluye la cabecera interna para autorizarse como
   * servicio confiable.
   */
  @Bean
  ClientServiceClient clientServiceClient(RestClient.Builder restClientBuilder) {
    RestClient restClient =
        restClientBuilder
            .clone()
            .baseUrl(servicesProperties.getMap().get("sgivu-client").getUrl())
            .defaultHeader("X-Internal-Service-Key", internalServiceKey)
            .build();

    RestClientAdapter adapter = RestClientAdapter.create(restClient);
    HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
    return factory.createClient(ClientServiceClient.class);
  }

  /** Proxy hacia el microservicio de usuarios (gestores internos). */
  @Bean
  UserServiceClient userServiceClient(RestClient.Builder restClientBuilder) {
    RestClient restClient =
        restClientBuilder
            .clone()
            .baseUrl(servicesProperties.getMap().get("sgivu-user").getUrl())
            .defaultHeader("X-Internal-Service-Key", internalServiceKey)
            .build();

    RestClientAdapter adapter = RestClientAdapter.create(restClient);
    HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
    return factory.createClient(UserServiceClient.class);
  }

  /** Proxy hacia el microservicio de inventario de vehículos usados. */
  @Bean
  VehicleServiceClient vehicleServiceClient(RestClient.Builder restClientBuilder) {
    RestClient restClient =
        restClientBuilder
            .clone()
            .baseUrl(servicesProperties.getMap().get("sgivu-vehicle").getUrl())
            .defaultHeader("X-Internal-Service-Key", internalServiceKey)
            .build();

    RestClientAdapter adapter = RestClientAdapter.create(restClient);
    HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
    return factory.createClient(VehicleServiceClient.class);
  }
}
