package test.APITesting;

import io.restassured.RestAssured;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ShowImageTest {
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/shopping_cart_war";
    }

    @Test
    void testShowImageWithValidProductId() throws IOException {
        String productId = "P20230423082243";
        // get the local iphone image
        File file = new File("./WebContent/images/iphone.png");
        byte[] expectedBytes = FileUtils.readFileToByteArray(file);
        // get the image for the iphone from the ShowImage function
        byte[] responseBytes = given()
                .param("pid", productId)
                .when()
                .get("/ShowImage")
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();

        assertArrayEquals(expectedBytes, responseBytes);
    }

    @Test
    void testShowImageWithInvalidProductId() throws IOException {
        String productId = "invalidId";
        // get the local no image picture
        File file = new File("./WebContent/images/output_image.jpg");
        byte[] expectedBytes = FileUtils.readFileToByteArray(file);
        // get the image for invalid product id, exepcted to be the no image picture
        byte[] responseBytes = given()
                .param("pid", productId)
                .when()
                .get("/ShowImage")
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();

        assertArrayEquals(expectedBytes, responseBytes);
    }
}
