package academy.devdojo.controller;

import academy.devdojo.commons.FileUtils;
import academy.devdojo.config.IntegrationTestConfig;
import academy.devdojo.config.RestAssuredConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.wiremock.spring.ConfigureWireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = RestAssuredConfig.class)
@Sql(value = "/sql/user/init_one_login_regular_user.sql")
@Sql(value = "/sql/user/clean_users.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ConfigureWireMock(port = 0, filesUnderClasspath = "wiremock/brasil-api/cep")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BrasilApiControllerIT extends IntegrationTestConfig {

  private static final String URL = "v1/brasil-api/cep";
  @Autowired
  private FileUtils fileUtils;

  @Autowired
  @Qualifier(value = "requestSpecificationRegularUser")
  private RequestSpecification requestSpecificationRegularUser;

  @BeforeEach
  void setUrl() {
    RestAssured.requestSpecification = requestSpecificationRegularUser;
  }

  @Order(1)
  @Test
  @DisplayName("findCep returns CepGetResponse when successful")
  void findCep_ReturnsCepGetResponse_WhenSuccessful() {
    var cep = "00000000";
    var expectedResponse = fileUtils.readResourceFile("brasil-api/cep/expected-get-cep-response-200.json");

    RestAssured.given()
        .contentType(ContentType.JSON).accept(ContentType.JSON)
        .when()
        .get(URL + "/{cep}", cep)
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(Matchers.equalTo(expectedResponse))
        .log().all();
  }

  @Order(2)
  @Test
  @DisplayName("findCep returns CepErrorResponse when unsuccessful")
  void findCep_ReturnsCepErrorResponse_WhenUnsuccessful() {
    var cep = "40400000";
    var expectedResponse = fileUtils.readResourceFile("brasil-api/cep/expected-get-cep-response-404.json");

    RestAssured.given()
        .contentType(ContentType.JSON).accept(ContentType.JSON)
        .when()
        .get(URL + "/{cep}", cep)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .body(Matchers.equalTo(expectedResponse))
        .log().all();
  }
}
