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

class IndexTest {
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
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testSearch() {
        // look for "phone" and hit enter, see results
        WebElement searchInput = driver.findElement(By.name("search"));
        searchInput.sendKeys("phone");
        WebElement searchButton = driver.findElement(By.cssSelector("input.btn.btn-danger[value='Search']"));
        searchButton.click();
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("Showing Results for 'phone'"));
    }

    @Test
    void testLogout() {
        // click logout button
        WebElement logoutButton = driver.findElement(By.cssSelector("a[href='./LogoutSrv']"));
        logoutButton.click();
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("Successfully Logged Out!"));
    }

    @Test
    void testAddToCart() {
        // buy the first default item
        WebElement addToCartButton = driver.findElement(By.cssSelector("button[type='submit'][formaction*='pid=P20230423082243'][class='btn btn-success']"));
        addToCartButton.click();
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("Product Successfully Updated to Cart!"));
    }

    @Test
    void testBuyNow() {
        // buy the first default item
        WebElement addToCartButton = driver.findElement(By.cssSelector("button[type='submit'][formaction*='pid=P20230423082243'][class='btn btn-primary']"));
        addToCartButton.click();
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("Product Successfully Updated to Cart!"));
    }

    @Test
    void testHomeIconRefresh() {
        driver.get(BASE_URL);
        WebElement homeIcon = driver.findElement(By.cssSelector("[class='glyphicon glyphicon-home']"));
        homeIcon.click();
        assertEquals("http://localhost:8080/shopping_cart_war/userHome.jsp", driver.getCurrentUrl());
    }

    @Test
    void removeFromCart() {
        // buy the first default item
        driver.findElement(By.cssSelector("button[type='submit'][formaction*='pid=P20230423082243']" +
                "[class='btn btn-primary']")).click();
        // now remove
        driver.findElement(By.cssSelector("button[type='submit'][formaction*='pid=P20230423082243']" +
                "[class='btn btn-danger']")).click();

        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("Product Successfully removed from the Cart!"));
    }

    @Test
    void testContactUs() {
        driver.get(BASE_URL);
        WebElement nameInput = driver.findElement(By.id("name"));
        nameInput.sendKeys("John Doe");
        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys("johndoe@example.com");
        WebElement messageInput = driver.findElement(By.id("comments"));
        messageInput.sendKeys("This is a test message.");
        WebElement sendButton = driver.findElement(By.cssSelector("button[class='btn pull-right']"));
        sendButton.click();
        // Wait for the popup to appear
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String popupMessage = driver.switchTo().alert().getText();
        assertEquals("Comments Sent Successfully", popupMessage);
    }

    @Test
    void testContactUsEmptyFields() {
        driver.get(BASE_URL);
        WebElement nameInput = driver.findElement(By.id("name"));
        nameInput.sendKeys("");
        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys("johndoe@example.com");
        WebElement messageInput = driver.findElement(By.id("comments"));
        messageInput.sendKeys("This is a test message.");
        WebElement sendButton = driver.findElement(By.cssSelector("button[class='btn pull-right']"));
        sendButton.click();
        // look for message
        String usernamePopupText = driver.findElement(By.id("name")).getAttribute("validationMessage");
        assertEquals("Please fill out this field.", usernamePopupText);
    }

    @Test
    void testContactUsInvalidEmail() {
        driver.get(BASE_URL);
        WebElement nameInput = driver.findElement(By.id("name"));
        nameInput.sendKeys("aasdsd");
        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys("asd");
        WebElement messageInput = driver.findElement(By.id("comments"));
        messageInput.sendKeys("This is a test message.");
        WebElement sendButton = driver.findElement(By.cssSelector("button[class='btn pull-right']"));
        sendButton.click();
        // look for message
        String emailPopupText = driver.findElement(By.name("email")).getAttribute("validationMessage");
        assert(emailPopupText.contains("Please include an '@' in the email address."));
    }
}