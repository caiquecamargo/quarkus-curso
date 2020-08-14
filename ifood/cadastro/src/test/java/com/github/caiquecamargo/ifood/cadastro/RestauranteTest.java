package com.github.caiquecamargo.ifood.cadastro;

import org.junit.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import com.github.database.rider.cdi.api.DBRider;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.configuration.Orthography;
import com.github.database.rider.core.api.dataset.DataSet;

import org.approvaltests.Approvals;

@DBRider
@DBUnit(caseInsensitiveStrategy = Orthography.LOWERCASE)
@QuarkusTest
@QuarkusTestResource(CadastroTestLifecycleManager.class)
public class RestauranteTest {

  @Test
  @DataSet("restaurante-cenario-1.yml")
  public void testBuscaRestaurantes() {
    String resultado = given().when().get("/restaurantes").then().statusCode(200).extract().asString();
    Approvals.verifyJson(resultado);
  }

}