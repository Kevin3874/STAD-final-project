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

class CheckoutTest {
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private WebDriver driver;
    private Actions actions;
    private String BASE_URL = "http://localhost:8080/shopping_cart_war/login.jsp";

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
        // Login first
        driver.findElement(By.name("username")).sendKeys("guest@gmail.com");
        driver.findElement(By.name("password")).sendKeys("guest");
        driver.findElement(By.cssSelector("button[type='submit'][class='btn btn-success']")).click();
        driver.get("http://localhost:8080/shopping_cart_war/payment.jsp?amount=1299.0");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testCheckoutInfo() {
        String name = generateRandomString(5);
        String card = generateRandomNumber(16);
        String month = "12";
        String year = "1234";
        String pin = "123";
        // enter info
        driver.findElement(By.name("cardholder")).sendKeys(name);
        driver.findElement(By.name("cardnumber")).sendKeys(card);
        driver.findElement(By.name("expmonth")).sendKeys(month);
        driver.findElement(By.name("expyear")).sendKeys(year);
        driver.findElement(By.cssSelector("input[type='number'][placeholder='123']")).sendKeys(pin);
        WebElement searchButton = driver.findElement(By.cssSelector("button[type='submit'][class='form-control btn btn-success']"));
        searchButton.click();
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("Order Placement Failed!"));
    }

    @Test
    void testMissingField() {
        String name = "";
        String card = generateRandomNumber(16);
        String month = "12";
        String year = "1234";
        String pin = "123";
        // enter info
        driver.findElement(By.name("cardholder")).sendKeys(name);
        driver.findElement(By.name("cardnumber")).sendKeys(card);
        driver.findElement(By.name("expmonth")).sendKeys(month);
        driver.findElement(By.name("expyear")).sendKeys(year);
        driver.findElement(By.cssSelector("input[type='number'][placeholder='123']")).sendKeys(pin);
        WebElement searchButton = driver.findElement(By.cssSelector("button[type='submit'][class='form-control btn btn-success']"));
        searchButton.click();
        String usernamePopupText = driver.findElement(By.name("cardholder")).getAttribute("validationMessage");
        assertEquals("Please fill out this field.", usernamePopupText);
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