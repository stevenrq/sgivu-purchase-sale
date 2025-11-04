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

@Configuration
public class AppConfig {

  @Value("${service.internal.secret-key}")
  private String internalServiceKey;

  private final ServicesProperties servicesProperties;

  public AppConfig(ServicesProperties servicesProperties) {
    this.servicesProperties = servicesProperties;
  }

  @Bean
  @LoadBalanced
  RestClient.Builder restClientBuilder(JwtAuthorizationInterceptor jwtAuthorizationInterceptor) {
    return RestClient.builder().requestInterceptor(jwtAuthorizationInterceptor);
  }

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
