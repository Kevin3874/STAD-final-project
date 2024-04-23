package test.APITesting;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class FansMessageTest {
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/shopping_cart_war";
    }

    @Test
    void testSendFansMessageWithValidData() {
        given()
                .param("name", "John Doe")
                .param("email", "johndoe@example.com")
                .param("comments", "Great products and excellent service!")
                .when()
                .post("/fansMessage")
                .then()
                .statusCode(200)
                .body(containsString("Comments Sent Successfully"));
    }

    @Test
    void testSendFansMessageWithMissingName() {
        given()
                .param("email", "johndoe@example.com")
                .param("comments", "Great products and excellent service!")
                .when()
                .post("/fansMessage")
                .then()
                .statusCode(200)
                .body(containsString("Comments Sent Successfully"));
    }

    @Test
    void testSendFansMessageWithMissingEmail() {
        given()
                .param("name", "John Doe")
                .param("comments", "Great products and excellent service!")
                .when()
                .post("/fansMessage")
                .then()
                .statusCode(200)
                .body(containsString("Comments Sent Successfully"));
    }

    @Test
    void testSendFansMessageWithMissingComments() {
        given()
                .param("name", "John Doe")
                .param("email", "johndoe@example.com")
                .when()
                .post("/fansMessage")
                .then()
                .statusCode(200)
                .body(containsString("Comments Sent Successfully"));
    }
}