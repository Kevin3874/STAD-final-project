package test.APITesting;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class LogoutSrvTest {
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/shopping_cart_war";
    }

    @Test
    void testLogout() {
        // login first
        Response loginResponse = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "guest@gmail.com")
                .formParam("password", "guest")
                .formParam("usertype", "customer")
                .when()
                .post("/LoginSrv")
                .then()
                .statusCode(200)
                .extract().response();
        String sessionId = loginResponse.getCookie("JSESSIONID");

        // Perform logout using the obtained session ID
        given()
                .cookie("JSESSIONID", sessionId)
                .when()
                .post("/LogoutSrv")
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("text/html"))
                .body(containsString("Successfully Logged Out!"));
    }
}