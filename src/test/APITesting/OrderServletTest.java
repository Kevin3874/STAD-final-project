package test.APITesting;

import com.shashi.service.impl.OrderServiceImpl;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OrderServletTest {
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/shopping_cart_war";
    }

    @Test
    void testPlaceOrderAmountWrong() {
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

        // Place an order with a valid session
        given()
                .cookie("JSESSIONID", sessionId)
                .param("amount", "1000.0")
                .when()
                .post("/OrderServlet")
                .then()
                .statusCode(200)
                .body(containsString("Order Placement Failed!"));
    }



    @Test
    void testPlaceOrderWithInvalidSession() {
        // Try to place an order without a valid session
        given()
                .param("amount", "100.0")
                .when()
                .post("/OrderServlet")
                .then()
                .statusCode(302)
                .header("Location", containsString("Session Expired, Login Again!!"));
    }

    @Test
    void testPlaceOrderWithAddedProduct() {
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
                .multiPart("name", "Test Product Order Help")
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

        given()
                .cookie("JSESSIONID", adminSessionId)
                .when()
                .post("/LogoutSrv")
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("text/html"))
                .body(containsString("Successfully Logged Out!"));

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
        System.out.println("ProductId: " + productId);
        // Add the product to the cart using the customer session
        given()
                .cookie("JSESSIONID", customerSessionId)
                .param("pid", productId)
                .param("pqty", "1")
                .when()
                .post("/AddtoCart")
                .then()
                .statusCode(200)
                .body(containsString("Product Successfully Updated to Cart!"));

        // Place an order with the total amount
        given()
                .cookie("JSESSIONID", customerSessionId)
                .param("amount", "100.00")
                .when()
                .post("/OrderServlet")
                .then()
                .statusCode(200)
                .body(containsString("Order Placed Successfully!"));
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