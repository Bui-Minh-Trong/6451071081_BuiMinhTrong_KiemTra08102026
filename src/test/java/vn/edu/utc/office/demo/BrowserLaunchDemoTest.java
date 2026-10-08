package vn.edu.utc.office.demo;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

/**
 * Smoke Test Demo: Validates browser initialization, navigation, and cleanup.
 * Designed to ensure the Selenium WebDriver environment is functioning correctly.
 * Supports execution via JUnit 5, TestNG, or main method.
 */
public class BrowserLaunchDemoTest {

    @org.junit.jupiter.api.Test
    @org.testng.annotations.Test
    public void testOpenAndCloseBrowser() {
        runDemo();
    }

    public static void main(String[] args) {
        runDemo();
    }

    private static void runDemo() {
        System.out.println("==================================================");
        System.out.println("  [DEMO] Starting Selenium WebDriver Smoke Test   ");
        System.out.println("==================================================");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-notifications");
        options.addArguments("--start-maximized");

        WebDriver driver = null;

        try {
            System.out.println("[STEP 1] Launching Google Chrome browser...");
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            String targetUrl = "https://vanphongdientu.utc.edu.vn/";
            System.out.println("[STEP 2] Navigating to: " + targetUrl);
            driver.get(targetUrl);

            String pageTitle = driver.getTitle();
            String currentUrl = driver.getCurrentUrl();
            System.out.println("[STEP 3] Successfully loaded page.");
            System.out.println("         -> Page Title: " + pageTitle);
            System.out.println("         -> Current URL: " + currentUrl);

            System.out.println("[STEP 4] Pausing for 3 seconds for visual inspection...");
            Thread.sleep(3000);

        } catch (InterruptedException e) {
            System.err.println("[ERROR] Thread sleep interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error during browser automation: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) {
                System.out.println("[STEP 5] Closing browser and terminating ChromeDriver process...");
                driver.quit();
                System.out.println("[STATUS] Browser closed cleanly.");
            }
            System.out.println("==================================================");
            System.out.println("  [DEMO] Smoke Test Completed Successfully        ");
            System.out.println("==================================================");
        }
    }
}
