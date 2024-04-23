package test.APITesting;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class LoginSrvTest {
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/shopping_cart_war/LoginSrv";
    }

    @Test
    void testAdminLogin() {
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "admin@gmail.com")
                .formParam("password", "admin")
                .formParam("usertype", "admin")
                .when()
                .post()
                .then()
                .statusCode(200)
                .cookie("JSESSIONID", not(emptyString()))
                .header("Content-Type", containsString("text/html"))
                .body(containsString("adminViewProduct.jsp"));
    }

    @Test
    void testAdminLoginWithInvalidCredentials() {
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "admin@gmail.com")
                .formParam("password", "wrongpassword")
                .formParam("usertype", "admin")
                .when()
                .post()
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("text/html"))
                .body(containsString("Login Denied! Invalid Username or password."));
    }

    @Test
    void testCustomerLoginWithValidCredentials() {
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "guest@gmail.com")
                .formParam("password", "guest")
                .formParam("usertype", "customer")
                .when()
                .post()
                .then()
                .statusCode(200)
                .cookie("JSESSIONID", not(emptyString()))
                .header("Content-Type", containsString("text/html"))
                .body(containsString("userHome.jsp"));
    }

    @Test
    void testCustomerLoginWithInvalidCredentials() {
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "invaliduser@example.com")
                .formParam("password", "invalidpassword")
                .formParam("usertype", "customer")
                .when()
                .post()
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("text/html"))
                .body(containsString("Login Denied! Incorrect Username or Password"));
    }
}