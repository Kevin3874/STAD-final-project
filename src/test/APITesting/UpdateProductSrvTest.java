package test.APITesting;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UpdateProductSrvTest {
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/shopping_cart_war";
    }

    @Test
    void testUpdateProductWithValidAdminLogin() {
        // first admin login
        Response loginResponse = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "admin@gmail.com")
                .formParam("password", "admin")
                .formParam("usertype", "admin")
                .when()
                .post("/LoginSrv")
                .then()
                .statusCode(200)
                .extract().response();

        String sessionId = loginResponse.getCookie("JSESSIONID");

        // use default products
        String productId = "P20230423084148";

        given()
                .cookie("JSESSIONID", sessionId)
                .param("pid", productId)
                .param("name", "Updated Product")
                .param("type", "Updated Type")
                .param("info", "Updated Info")
                .param("price", "19.99")
                .param("quantity", "10")
                .when()
                .post("/UpdateProductSrv")
                .then()
                .statusCode(200)
                .body(containsString("Product Updated Successfully"));
    }

    @Test
    void testUpdateProductWithInvalidLogin() {
        // no login info
        String productId = "P123";

        given()
                .param("pid", productId)
                .param("name", "Updated Product")
                .param("type", "Updated Type")
                .param("info", "Updated Info")
                .param("price", "19.99")
                .param("quantity", "10")
                .when()
                .post("/UpdateProductSrv")
                .then()
                .statusCode(302)
                .header("Location", containsString("Access Denied, Login As Admin!!"));
    }

    @Test
    void testUpdateProductWithExpiredSession() {
        // first admin login
        Response loginResponse = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "admin@gmail.com")
                .formParam("password", "admin")
                .formParam("usertype", "admin")
                .when()
                .post("/LoginSrv")
                .then()
                .statusCode(200)
                .extract().response();

        String sessionId = loginResponse.getCookie("JSESSIONID");

        // log out, reset cookies
        given()
                .cookie("JSESSIONID", sessionId)
                .when()
                .get("/LogoutSrv")
                .then()
                .statusCode(200);

        String productId = "P123";

        given()
                .cookie("JSESSIONID", sessionId)
                .param("pid", productId)
                .param("name", "Updated Product")
                .param("type", "Updated Type")
                .param("info", "Updated Info")
                .param("price", "19.99")
                .param("quantity", "10")
                .when()
                .post("/UpdateProductSrv")
                .then()
                .statusCode(302)
                .header("Location", containsString("Session Expired, Login Again!!"));
    }
}