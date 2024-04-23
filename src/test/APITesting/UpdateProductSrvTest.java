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
        // Perform admin login to obtain a valid session ID
        Response loginResponse = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "admin@example.com")
                .formParam("password", "adminpassword")
                .formParam("usertype", "admin")
                .when()
                .post("/LoginSrv")
                .then()
                .statusCode(200)
                .extract().response();

        String sessionId = loginResponse.getCookie("JSESSIONID");

        // Update a product with valid parameters
        String productId = "P123"; // Replace with a valid product ID

        given()
                .cookie("JSESSIONID", sessionId)
                .param("pid", productId)
                .param("name", "Updated Product")
                .param("type", "Updated Type")
                .param("info", "Updated Info")
                .param("price", "19.99")
                .param("quantity", "10")
                .when()
                .get("/UpdateProductSrv")
                .then()
                .statusCode(200)
                .header("Location", containsString("updateProduct.jsp?prodid=" + productId + "&message=Product Updated Successfully"));
    }

    @Test
    void testUpdateProductWithInvalidLogin() {
        // Update a product without a valid login
        String productId = "P123"; // Replace with a valid product ID

        given()
                .param("pid", productId)
                .param("name", "Updated Product")
                .param("type", "Updated Type")
                .param("info", "Updated Info")
                .param("price", "19.99")
                .param("quantity", "10")
                .when()
                .get("/UpdateProductSrv")
                .then()
                .statusCode(302)
                .header("Location", containsString("login.jsp?message=Access Denied, Login As Admin!!"));
    }

    @Test
    void testUpdateProductWithExpiredSession() {
        // Perform admin login to obtain a valid session ID
        Response loginResponse = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "admin@example.com")
                .formParam("password", "adminpassword")
                .formParam("usertype", "admin")
                .when()
                .post("/LoginSrv")
                .then()
                .statusCode(200)
                .extract().response();

        String sessionId = loginResponse.getCookie("JSESSIONID");

        // Invalidate the session by logging out
        given()
                .cookie("JSESSIONID", sessionId)
                .when()
                .get("/LogoutSrv")
                .then()
                .statusCode(200);

        // Update a product with an expired session
        String productId = "P123"; // Replace with a valid product ID

        given()
                .cookie("JSESSIONID", sessionId)
                .param("pid", productId)
                .param("name", "Updated Product")
                .param("type", "Updated Type")
                .param("info", "Updated Info")
                .param("price", "19.99")
                .param("quantity", "10")
                .when()
                .get("/UpdateProductSrv")
                .then()
                .statusCode(302)
                .header("Location", containsString("login.jsp?message=Session Expired, Login Again!!"));
    }
}