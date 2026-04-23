package com.enuygun.api.tests;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.http.ContentType;
import io.restassured.internal.http.HttpResponseException;
import io.restassured.response.Response;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;

public class PetstorePetCrudTest extends BaseApiTest {

  @Test
  void pet_crud_positive_flow_with_schema_validation() {
    long petId = Instant.now().toEpochMilli();

    Map<String, Object> category = new HashMap<>();
    category.put("id", 1);
    category.put("name", "cats");

    Map<String, Object> tag = new HashMap<>();
    tag.put("id", 1);
    tag.put("name", "tag1");

    Map<String, Object> create = new HashMap<>();
    create.put("id", petId);
    create.put("category", category);
    create.put("name", "enuygun-pet");
    create.put("photoUrls", List.of("https://example.com/1.png"));
    create.put("tags", List.of(tag));
    create.put("status", "available");

    long createdId =
        given()
            .log()
            .all()
            .contentType(ContentType.JSON)
            .body(create)
            .when()
            .post("/pet")
            .then()
            .log()
            .all()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("schemas/petstore/pet.json"))
            .extract()
            .path("id");

    assertThat(createdId).isEqualTo(petId);

    String gotName =
        given()
            .log()
            .all()
            .when()
            .get("/pet/{petId}", petId)
            .then()
            .log()
            .all()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("schemas/petstore/pet.json"))
            .extract()
            .path("name");

    assertThat(gotName).isEqualTo("enuygun-pet");

    Map<String, Object> update = new HashMap<>(create);
    update.put("name", "enuygun-pet-updated");
    update.put("status", "sold");

    String updatedName =
        given()
            .log()
            .all()
            .contentType(ContentType.JSON)
            .body(update)
            .when()
            .put("/pet")
            .then()
            .log()
            .all()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("schemas/petstore/pet.json"))
            .extract()
            .path("name");

    assertThat(updatedName).isEqualTo("enuygun-pet-updated");

    given()
        .log()
        .all()
        .when()
        .delete("/pet/{petId}", petId)
        .then()
        .log()
        .all()
        .statusCode(200)
        .body(matchesJsonSchemaInClasspath("schemas/petstore/api-response.json"));

    assertStatusCodeWithSchema(
        404,
        "schemas/petstore/api-response.json",
        () -> given().log().all().when().get("/pet/{petId}", petId));

    assertStatusCodeWithSchema(
        404,
        "schemas/petstore/api-response.json",
        () -> given().log().all().when().delete("/pet/{petId}", petId));
  }

  @Test
  void get_pet_negative_non_existing_id_returns_404() {
    long missingId = 999_999_999_999L;
    assertStatusCodeWithSchema(
        404,
        "schemas/petstore/api-response.json",
        () -> given().log().all().when().get("/pet/{petId}", missingId));
  }

  @Test
  void create_pet_negative_malformed_json_returns_400() {
    String malformed = "{\"id\": 1, \"name\": ";
    assertAnyStatusCode(
        new int[] {400, 405},
        () ->
            given()
                .log()
                .all()
                .contentType(ContentType.JSON)
                .body(malformed)
                .when()
                .post("/pet"));
  }

  @Test
  void get_pet_negative_invalid_id_returns_400() {
    assertAnyStatusCode(
        new int[] {400, 404},
        () -> given().log().all().when().get("/pet/{petId}", "not-a-number"));
  }

  @Test
  void update_pet_negative_malformed_json_returns_400_or_405() {
    String malformed = "{\"id\": 1, \"name\": ";
    assertAnyStatusCode(
        new int[] {400, 405},
        () ->
            given()
                .log()
                .all()
                .contentType(ContentType.JSON)
                .body(malformed)
                .when()
                .put("/pet"));
  }

  @FunctionalInterface
  private interface ResponseCall {
    Response call() throws Exception;
  }

  private static void assertStatusCode(int expected, ResponseCall call) {
    try {
      Response r = call.call();
      int actual = r.getStatusCode();
      assertThat(actual).isEqualTo(expected);
    } catch (Exception e) {
      if (e instanceof HttpResponseException hre) {
        assertThat(hre.getStatusCode()).isEqualTo(expected);
        return;
      }
      throw new RuntimeException(e);
    }
  }

  private static void assertAnyStatusCode(int[] expected, ResponseCall call) {
    try {
      Response r = call.call();
      int actual = r.getStatusCode();
      boolean ok = false;
      for (int e : expected) {
        if (actual == e) {
          ok = true;
          break;
        }
      }
      assertThat(ok).as("Actual status %s expected one of %s", actual, java.util.Arrays.toString(expected)).isTrue();
    } catch (Exception e) {
      if (e instanceof HttpResponseException hre) {
        int actual = hre.getStatusCode();
        boolean ok = false;
        for (int ex : expected) {
          if (actual == ex) {
            ok = true;
            break;
          }
        }
        assertThat(ok).as("Actual status %s expected one of %s", actual, java.util.Arrays.toString(expected)).isTrue();
        return;
      }
      throw new RuntimeException(e);
    }
  }

  private static void assertStatusCodeWithSchema(int expected, String schemaPath, ResponseCall call) {
    try {
      Response r = call.call();
      assertThat(r.getStatusCode()).isEqualTo(expected);
      r.then().log().all().body(matchesJsonSchemaInClasspath(schemaPath));
    } catch (Exception e) {
      if (e instanceof HttpResponseException hre) {
        assertThat(hre.getStatusCode()).isEqualTo(expected);
        return;
      }
      throw new RuntimeException(e);
    }
  }
}

