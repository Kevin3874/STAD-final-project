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

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

class LoginTest {
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
        driver.get("http://localhost:8080/shopping_cart_war/login.jsp");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testLoginSuccessful() {
        // find fields
        driver.findElement(By.name("username")).sendKeys("guest@gmail.com");
        driver.findElement(By.name("password")).sendKeys("guest");
        driver.findElement(By.cssSelector("button[type='submit'][class='btn btn-success']")).click();
        String bodyText = driver.findElement(By.tagName("body")).getText();
        // we are now in homepage successfuly
        assert(bodyText.contains("All Products"));
    }

    @Test
    void testLoginFailed() {
        // find fields
        driver.findElement(By.name("username")).sendKeys("wrgonusername@gmail.com");
        driver.findElement(By.name("password")).sendKeys("guest");
        driver.findElement(By.cssSelector("button[type='submit'][class='btn btn-success']")).click();
        String bodyText = driver.findElement(By.tagName("body")).getText();
        // still in login page, wrong login
        assert(bodyText.contains("Login Denied! Incorrect Username or Password"));
    }

    @Test
    void testLoginWithBlankFields() {
        // find fields
        driver.findElement(By.name("username")).sendKeys("");
        driver.findElement(By.name("password")).sendKeys("");
        driver.findElement(By.cssSelector("button[type='submit'][class='btn btn-success']")).click();

        // Check if the "Please fill out this field" popup is displayed for the username field
        String usernamePopupText = driver.findElement(By.name("username")).getAttribute("validationMessage");
        assertEquals("Please fill out this field.", usernamePopupText);
    }

    @Test
    void testLoginWithInvalidEmail() {
        driver.get("http://localhost:8080/shopping_cart_war/login.jsp");
        // take them to login page
        driver.findElement(By.name("username")).sendKeys("hello");
        driver.findElement(By.name("password")).sendKeys("guest");
        driver.findElement(By.cssSelector("button[type='submit'][class='btn btn-success']")).click();

        // Check if the "Please include an '@' in the email address" popup is displayed
        String emailPopupText = driver.findElement(By.name("username")).getAttribute("validationMessage");
        assert(emailPopupText.contains("Please include an '@' in the email address."));
    }
}