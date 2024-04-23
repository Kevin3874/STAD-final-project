package test.APITesting;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AddProductSrvTest {
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/shopping_cart_war";
    }


    @Test
    void testAddProductWithValidAdminLogin() {
        // admin login
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

        // add admin cookies
        given()
                .cookie("JSESSIONID", sessionId)
                .contentType("multipart/form-data")
                .multiPart("name", "Test add product")
                .multiPart("type", "Sample Type")
                .multiPart("info", "Sample Info")
                .multiPart("price", "10.99")
                .multiPart("quantity", "1000000")
                .multiPart("image", new File("./WebContent/images/image.jpg"))
                .when()
                .post("/AddProductSrv")
                .then()
                .statusCode(200)
                .body(containsString("Product Added Successfully with Product Id:"));
    }

    @Test
    void testAddProductWithInvalidLogin() {
        given()
                .when()
                .post("/AddProductSrv")
                .then()
                .statusCode(500)
                .header("Location", containsString("login.jsp?message=Access Denied!"));
    }

    @Test
    void testAddProductWithExpiredSession() {
        Response loginResponse = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "customer@example.com")
                .formParam("password", "customerpassword")
                .formParam("usertype", "customer")
                .when()
                .post("/LoginSrv")
                .then()
                .statusCode(200)
                .extract().response();

        String sessionId = loginResponse.getCookie("JSESSIONID");

        // Invalidate the session
        given()
                .cookie("JSESSIONID", sessionId)
                .when()
                .get("/LogoutSrv")
                .then()
                .statusCode(200);
        // empty username/pass
        given()
                .cookie("JSESSIONID", sessionId)
                .param("usertype", "admin")
                .param("username", "")
                .param("password", "")
                .when()
                .post("/AddProductSrv")
                .then()
                .statusCode(500)
                .header("Location", containsString("Access Denied!"));
    }
}