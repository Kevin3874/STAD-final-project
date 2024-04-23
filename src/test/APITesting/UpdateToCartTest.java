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

public class UpdateToCartTest {
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/shopping_cart_war";
    }

    @Test
    void testUpdateToCartWithSufficientStock() {
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

        // Extract the product ID from the response
        String responseBody = addProductResponse.getBody().asString();
        String productId = extractProductId(responseBody);

        // Perform customer login to obtain a valid session ID
        Response customerLoginResponse = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "guest@gmail.com")
                .formParam("password", "guest")
                .formParam("usertype", "customer")
                .when()
                .post("/LoginSrv")
                .then()
                .statusCode(200)
                .extract().response();

        String customerSessionId = customerLoginResponse.getCookie("JSESSIONID");

        // Update the quantity of the product in the cart
        given()
                .cookie("JSESSIONID", customerSessionId)
                .param("pid", productId)
                .param("pqty", "3")
                .when()
                .post("/UpdateToCart")
                .then()
                .statusCode(200)
                .body(containsString("Product Successfully Updated to Cart!"));
    }

    @Test
    void testUpdateToCartWithInsufficientStock() {
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
        // Extract the product ID from the response
        String responseBody = addProductResponse.getBody().asString();
        String productId = extractProductId(responseBody);

        // Perform customer login to obtain a valid session ID
        Response customerLoginResponse = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "guest@gmail.com")
                .formParam("password", "guest")
                .formParam("usertype", "customer")
                .when()
                .post("/LoginSrv")
                .then()
                .statusCode(200)
                .extract().response();

        String customerSessionId = customerLoginResponse.getCookie("JSESSIONID");


        // Update the quantity of the product in the cart with insufficient stock
        given()
                .cookie("JSESSIONID", customerSessionId)
                .param("pid", productId)
                .param("pqty", "10000")
                .when()
                .post("/UpdateToCart")
                .then()
                .statusCode(200)
                .body(containsString("Only 5 no of Test Product are available in the shop!"));
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