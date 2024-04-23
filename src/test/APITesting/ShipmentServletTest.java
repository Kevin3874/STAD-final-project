package test.APITesting;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

public class ShipmentServletTest {
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/shopping_cart_war";
    }

    @Test
    void testShipmentWithInvalidSession() {
        // Ship an order without a valid session
        given()
                .param("orderid", "O123")
                .param("prodid", "P456")
                .param("userid", "user@example.com")
                .param("amount", "100.0")
                .when()
                .post("/ShipmentServlet")
                .then()
                .statusCode(302)
                .header("Location", containsString("login.jsp?message=Access Denied, Login Again!!"));
    }


    /*
    * To test, we need to go through all the steps of adding to cart and checking out fisrt
     */
    @Test
    void testShipmentWithValidOrder() {
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

        // use default iphone, there are 1000
        given()
                .cookie("JSESSIONID", customerSessionId)
                .param("pid", "P20230423082243")
                .param("pqty", "1")
                .when()
                .post("/AddtoCart")
                .then()
                .statusCode(200)
                .body(containsString("Product Successfully Updated to Cart!"));

        // Place an order
        Response orderResponse = given()
                .cookie("JSESSIONID", customerSessionId)
                .param("amount", "10.99")
                .when()
                .get("/OrderServlet")
                .then()
                .statusCode(200)
                .body(containsString("Order Placed Successfully!"))
                .extract().response();

        String orderHtml = orderResponse.getBody().asString();
        String orderId = extractOrderIdFromHtml(orderHtml);

        // Ship the order
        given()
                .cookie("JSESSIONID", customerSessionId)
                .param("orderid", orderId)
                .param("prodid", "P20230423082243")
                .param("userid", "customer@example.com")
                .param("amount", "10.99")
                .when()
                .get("/ShipmentServlet")
                .then()
                .statusCode(200)
                .body(containsString("Order Has been shipped successfully!!"));
    }

    @Test
    void testShipmentWithDuplicateShipment() {
        // Perform admin login to obtain a valid session ID
        Response adminLoginResponse = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "admin@example.com")
                .formParam("password", "adminpassword")
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
                .multiPart("image", new File("path/to/image/file.jpg"))
                .when()
                .post("/AddProductSrv")
                .then()
                .statusCode(200)
                .extract().response();

        String productId = extractProductId(addProductResponse.getBody().asString());

        // Perform customer login to obtain a valid session ID
        Response customerLoginResponse = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "customer@example.com")
                .formParam("password", "customerpassword")
                .formParam("usertype", "customer")
                .when()
                .post("/LoginSrv")
                .then()
                .statusCode(200)
                .extract().response();

        String customerSessionId = customerLoginResponse.getCookie("JSESSIONID");

        // Add the product to the cart
        given()
                .cookie("JSESSIONID", customerSessionId)
                .param("pid", productId)
                .param("pqty", "1")
                .when()
                .get("/AddtoCart")
                .then()
                .statusCode(200)
                .body(containsString("Product added to cart successfully!"));

        // Place an order
        Response orderResponse = given()
                .cookie("JSESSIONID", customerSessionId)
                .param("amount", "10.99")
                .when()
                .get("/OrderServlet")
                .then()
                .statusCode(200)
                .body(containsString("Payment Successful"))
                .extract().response();

        String orderId = extractOrderId(orderResponse.getBody().asString());

        // Ship the order
        given()
                .cookie("JSESSIONID", adminSessionId)
                .param("orderid", orderId)
                .param("prodid", productId)
                .param("userid", "customer@example.com")
                .param("amount", "10.99")
                .when()
                .get("/ShipmentServlet")
                .then()
                .statusCode(200)
                .body(containsString("Order Shipped Successfully"));

        // Attempt to ship the same order again
        given()
                .cookie("JSESSIONID", adminSessionId)
                .param("orderid", orderId)
                .param("prodid", productId)
                .param("userid", "customer@example.com")
                .param("amount", "10.99")
                .when()
                .get("/ShipmentServlet")
                .then()
                .statusCode(200)
                .body(containsString("Shipment Failed"));
    }

    private String extractProductId(String responseBody) {
        Pattern pattern = Pattern.compile("Product Added Successfully with Product Id: (P\\d+)");
        Matcher matcher = pattern.matcher(responseBody);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractOrderId(String responseBody) {
        Pattern pattern = Pattern.compile("Order Placed Successfully with Order Id: (O\\d+)");
        Matcher matcher = pattern.matcher(responseBody);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractOrderIdFromHtml(String html) {
        Document document = Jsoup.parse(html);
        Elements rows = document.select("table.table-hover tbody tr");
        if (!rows.isEmpty()) {
            Element lastRow = rows.last();
            Elements cells = lastRow.select("td");
            if (cells.size() >= 3) {
                return cells.get(2).text();
            }
        }
        return null;
    }
}
