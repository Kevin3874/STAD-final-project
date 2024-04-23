package test.APITesting;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AddToCartTest {
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/shopping_cart_war";
    }

    @Test
    void testAddToCartWithValidSession() {
        // login
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

        // Add a product to the cart
        given()
                .cookie("JSESSIONID", sessionId)
                .param("pid", "P20240422023722")
                .param("pqty", "1")
                .when()
                .get("/AddtoCart")
                .then()
                .statusCode(200)
                .body(containsString("Product Successfully Updated to Cart!"));
    }

    @Test
    void testAddToCartWithInvalidSession() {
        // Try to add a product to the cart without a valid session
        given()
                .param("pid", "1")
                .param("pqty", "2")
                .when()
                .post("/AddtoCart")
                .then()
                .statusCode(302)
                .header("Location", containsString("Session Expired, Login Again to Continue!"));
    }

    @Test
    void testAddToCartWithInsufficientStock() {
        // Perform customer login to obtain a valid session ID
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

        // add too many quantity
        given()
                .cookie("JSESSIONID", sessionId)
                .param("pid", "P20240422105002")
                .param("pqty", "10000")
                .when()
                .get("/AddtoCart")
                .then()
                .statusCode(200)
                .body(containsString("Product is Out of Stock!"));
    }
}