package test.APITesting;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class RegisterSrvTest {
    /*
    * Need to use random strings because we cannot register user twice, would be bad for testing.
     */
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/shopping_cart_war";
    }

    @Test
    void testRegisterWithValidData() {
        String username = generateRandomString(10);
        String mobile = generateRandomNumber(10);
        String email = generateRandomString(8) + "@gmail.com";
        String address = generateRandomString(15);
        String pincode = generateRandomNumber(5);
        String password = generateRandomString(8);

        given()
                .param("username", username)
                .param("mobile", mobile)
                .param("email", email)
                .param("address", address)
                .param("pincode", pincode)
                .param("password", password)
                .param("confirmPassword", password)
                .when()
                .get("/RegisterSrv")
                .then()
                .statusCode(200)
                .body(containsString("User Registered Successfully!"));
    }

    @Test
    void testRegisterWithMismatchedPasswords() {
        String username = generateRandomString(10);
        String mobile = generateRandomNumber(10);
        String email = generateRandomString(8) + "@gmail.com";
        String address = generateRandomString(15);
        String pincode = generateRandomNumber(5);
        String password = generateRandomString(8);
        String confirmPassword = generateRandomString(8);

        given()
                .param("username", username)
                .param("mobile", mobile)
                .param("email", email)
                .param("address", address)
                .param("pincode", pincode)
                .param("password", password)
                .param("confirmPassword", confirmPassword)
                .when()
                .get("/RegisterSrv")
                .then()
                .statusCode(200)
                .body(containsString("Password not matching!"));
    }

    @Test
    void testRegisterWithMissingRequiredFields() {
        String username = generateRandomString(10);
        String mobile = generateRandomNumber(10);
        String email = generateRandomString(8) + "@gmail.com";
        String address = generateRandomString(15);
        String pincode = generateRandomNumber(5);
        String password = generateRandomString(8);

        given()
                .param("username", username)
                .param("mobile", mobile)
                .param("email", email)
                .param("address", "")
                .param("pincode", pincode)
                .param("password", password)
                .param("confirmPassword", password)
                .when()
                .get("/RegisterSrv")
                .then()
                .statusCode(200)
                .body(containsString("User Registered Successfully!"));
    }

    private String generateRandomString(int length) {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(index));
        }
        return builder.toString();
    }

    private String generateRandomNumber(int length) {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int digit = random.nextInt(10);
            builder.append(digit);
        }
        return builder.toString();
    }
}