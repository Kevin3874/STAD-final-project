package test.GUITesting;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

class RegistrationTest {
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private WebDriver driver;
    private Actions actions;
    private String BASE_URL = "http://localhost:8080/shopping_cart_war/register.jsp";

    @BeforeEach
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "/Users/Kevin/Desktop/Programming/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        actions = new Actions(driver);
        driver.get(BASE_URL);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testRegistrationSuccessful() {
        String username = generateRandomString(10);
        String mobile = generateRandomNumber(10);
        String email = generateRandomString(8) + "@gmail.com";
        String address = generateRandomString(15);
        String pincode = generateRandomNumber(5);
        String password = generateRandomString(8);
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("address")).sendKeys(address);
        driver.findElement(By.name("mobile")).sendKeys(mobile);
        driver.findElement(By.name("pincode")).sendKeys(pincode);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.name("confirmPassword")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit'][class='btn btn-success']")).click();
        String bodyText = driver.findElement(By.tagName("body")).getText();
        // register success
        assert(bodyText.contains("User Registered Successfully!"));
    }

    @Test
    void testRegisterWithBlankFields() {
        // make username blank
        String username = generateRandomString(10);
        String mobile = generateRandomNumber(10);
        String email = generateRandomString(8) + "@gmail.com";
        String address = generateRandomString(15);
        String pincode = generateRandomNumber(5);
        String password = generateRandomString(8);
        driver.findElement(By.name("username")).sendKeys("");
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("address")).sendKeys(address);
        driver.findElement(By.name("mobile")).sendKeys(mobile);
        driver.findElement(By.name("pincode")).sendKeys(pincode);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.name("confirmPassword")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit'][class='btn btn-success']")).click();
        // Check if the "Please fill out this field" popup is displayed for the username field
        String usernamePopupText = driver.findElement(By.name("username")).getAttribute("validationMessage");
        assertEquals("Please fill out this field.", usernamePopupText);
    }

    @Test
    void testRegisterWithInvalidEmail() {
        // have a bad email without @
        String username = generateRandomString(10);
        String mobile = generateRandomNumber(10);
        String email = generateRandomString(8) + "@gmail.com";
        String address = generateRandomString(15);
        String pincode = generateRandomNumber(5);
        String password = generateRandomString(8);
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("email")).sendKeys("bademail");
        driver.findElement(By.name("address")).sendKeys(address);
        driver.findElement(By.name("mobile")).sendKeys(mobile);
        driver.findElement(By.name("pincode")).sendKeys(pincode);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.name("confirmPassword")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit'][class='btn btn-success']")).click();

        // Check if the "Please include an '@' in the email address" popup is displayed
        String emailPopupText = driver.findElement(By.name("email")).getAttribute("validationMessage");
        assert(emailPopupText.contains("Please include an '@' in the email address."));
    }


    @Test
    void testRegisterWithPasswordsNotMatched() {
        // use wrong password for second
        String username = generateRandomString(10);
        String mobile = generateRandomNumber(10);
        String email = generateRandomString(8) + "@gmail.com";
        String address = generateRandomString(15);
        String pincode = generateRandomNumber(5);
        String password = generateRandomString(8);
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("address")).sendKeys(address);
        driver.findElement(By.name("mobile")).sendKeys(mobile);
        driver.findElement(By.name("pincode")).sendKeys(pincode);
        driver.findElement(By.name("password")).sendKeys("1234");
        driver.findElement(By.name("confirmPassword")).sendKeys("5678");
        driver.findElement(By.cssSelector("button[type='submit'][class='btn btn-success']")).click();

        String bodyText = driver.findElement(By.tagName("body")).getText();
        // bad password matching
        assert(bodyText.contains("Password not matching!"));
    }


    @Test
    void testRegisterWithSameEmailTwice() {
        String username = generateRandomString(10);
        String mobile = generateRandomNumber(10);
        String email = generateRandomString(8) + "@gmail.com";
        String address = generateRandomString(15);
        String pincode = generateRandomNumber(5);
        String password = generateRandomString(8);
        // register first time
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("address")).sendKeys(address);
        driver.findElement(By.name("mobile")).sendKeys(mobile);
        driver.findElement(By.name("pincode")).sendKeys(pincode);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.name("confirmPassword")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit'][class='btn btn-success']")).click();
        // register second time same email
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("address")).sendKeys(address);
        driver.findElement(By.name("mobile")).sendKeys(mobile);
        driver.findElement(By.name("pincode")).sendKeys(pincode);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.name("confirmPassword")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit'][class='btn btn-success']")).click();

        String bodyText = driver.findElement(By.tagName("body")).getText();
        // cannot have duplicate registrations
        assert(bodyText.contains("Email Id Already Registered!"));
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