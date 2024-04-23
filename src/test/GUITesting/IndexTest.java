package test.GUITesting;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import static org.junit.jupiter.api.Assertions.*;

public class IndexTest {
    private WebDriver driver;
    private Actions actions;

    @BeforeEach
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "/Users/Kevin/Desktop/Programming/chromedriver");
        ChromeOptions options = new ChromeOptions();

        driver = new ChromeDriver(options);
        actions = new Actions(driver);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testSearchNoResults() {
        driver.get("http://localhost:8080/shopping_cart_war/index.jsp");
        // Find the search input by its name or id
        //WebElement searchInput = driver.findElement(By.name("search")); // Adjust this locator based on your HTML

        // Type "asd" into the search field
        //searchInput.sendKeys("asd");

//        // Find the search button by its class name and click it
//        WebElement searchButton = driver.findElement(By.className("btn-danger")); // Use the class to locate the search button
//        searchButton.click();
//
//        // Assuming there is a brief loading time, we might need to wait for the results to be displayed
//        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
//                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'No items found for the search')]"))
//        );
//
//        // Verify the no results message is displayed
//        WebElement noResultsMessage = driver.findElement(By.xpath("//*[contains(text(),'No items found for the search')]"));
//        assertTrue(noResultsMessage.getText().contains("No items found for the search 'asd'"));
    }
}
