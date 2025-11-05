package com.sgivu.purchasesale;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    properties = {
      "spring.cloud.config.enabled=false",
      "spring.cloud.config.fail-fast=false",
      "spring.cloud.config.import-check.enabled=false",
      "spring.config.import=",
      "eureka.client.enabled=false"
    })
@ActiveProfiles("test")
@Disabled("Se omite la carga completa de contexto mientras no exista un Config Server disponible en pruebas.")
class SgivuPurchaseSaleApplicationTests {

	@Test
	void contextLoads() {
	}

}
