package test.APITesting;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class RemoveProductSrvTest {
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/shopping_cart_war";
    }

    @Test
    void testRemoveProductWithValidAdminLogin() {
        // Perform admin login to obtain a valid session ID
        Response adminLoginResponse = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "admin@gmail.com")
                .formParam("password", "admin")
                .formParam("usertype", "admin")
                .when()
                .post("/LoginSrv")
                .then()
                .statusCode(200)
                .extract().response();

        String adminSessionId = adminLoginResponse.getCookie("JSESSIONID");

        // Add a new product using the admin session
        Response addProductResponse = given()
                .cookie("JSESSIONID", adminSessionId)
                .contentType("multipart/form-data")
                .multiPart("name", "Test Product")
                .multiPart("type", "Test Type")
                .multiPart("info", "Test Info")
                .multiPart("price", "10.99")
                .multiPart("quantity", "5")
                .multiPart("image", new File("./WebContent/images/image.jpg"))
                .when()
                .post("/AddProductSrv")
                .then()
                .statusCode(200)
                .extract().response();

        String responseBody = addProductResponse.getBody().asString();
        String productId = extractProductId(responseBody);

        // Remove the product using the admin session
        given()
                .cookie("JSESSIONID", adminSessionId)
                .param("prodid", productId)
                .when()
                .get("/RemoveProductSrv")
                .then()
                .statusCode(200)
                .body(containsString("Product Removed Successfully"));
    }

    @Test
    void testRemoveProductWithInvalidLogin() {
        given()
                .param("prodid", "P1")
                .when()
                .post("/RemoveProductSrv")
                .then()
                .statusCode(500)
                .header("Location", containsString("login.jsp?message=Access Denied, Login As Admin!!"));
    }

    @Test
    void testRemoveProductWithExpiredSession() {
        // Perform admin login to obtain a valid session ID
        Response adminLoginResponse = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "admin@gmail.com")
                .formParam("password", "admin")
                .formParam("usertype", "admin")
                .when()
                .post("/LoginSrv")
                .then()
                .statusCode(200)
                .extract().response();

        String adminSessionId = adminLoginResponse.getCookie("JSESSIONID");

        // Invalidate the session by logging out
        given()
                .cookie("JSESSIONID", adminSessionId)
                .when()
                .get("/LogoutSrv")
                .then()
                .statusCode(200);

        // Try to remove a product with the expired session
        given()
                .cookie("JSESSIONID", adminSessionId)
                .param("prodid", "P1")
                .when()
                .post("/RemoveProductSrv")
                .then()
                .statusCode(500)
                .header("Location", containsString("Access Denied, Login As Admin!!"));
    }

    private String extractProductId(String responseBody) {
        Pattern pattern = Pattern.compile("Product Added Successfully with Product Id: (P\\d+)");
        Matcher matcher = pattern.matcher(responseBody);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}