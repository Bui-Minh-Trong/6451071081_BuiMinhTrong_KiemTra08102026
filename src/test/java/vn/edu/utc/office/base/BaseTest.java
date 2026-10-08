package vn.edu.utc.office.base;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

/**
 * BaseTest manages the test lifecycle with JUnit 5: initializes WebDriver,
 * configures explicit/implicit timeouts, and handles headless mode.
 * Conforms to Slide Buoi 8 (pages 10, 48, and 63).
 */
public abstract class BaseTest {

    protected WebDriver driver;
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-notifications");

        // Supports --headless mode as required in Slide p.63 via: mvn test -Dheadless=true
        String headlessFlag = System.getProperty("headless");
        if ("true".equalsIgnoreCase(headlessFlag) || "true".equalsIgnoreCase(System.getenv("HEADLESS"))) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        } else {
            options.addArguments("--start-maximized");
        }

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driverThreadLocal.set(driver);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit(); // Releases ChromeDriver process cleanly
            driverThreadLocal.remove();
        }
    }
}
