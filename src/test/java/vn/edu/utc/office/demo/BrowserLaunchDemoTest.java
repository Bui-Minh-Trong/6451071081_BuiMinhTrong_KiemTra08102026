package vn.edu.utc.office.demo;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * Smoke Test Demo: Validates browser initialization, navigation, and cleanup.
 * Designed to ensure the Selenium WebDriver environment is functioning correctly.
 */
public class BrowserLaunchDemoTest {

    @Test
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

        // Configure ChromeOptions to handle standard browser launch behavior
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-notifications");
        options.addArguments("--start-maximized");

        // Initialize Google Chrome WebDriver instance
        // Note: Selenium 4.6+ automatically manages ChromeDriver binaries via Selenium Manager
        WebDriver driver = null;

        try {
            System.out.println("[STEP 1] Launching Google Chrome browser...");
            driver = new ChromeDriver(options);

            // Configure implicit wait timeout
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            // Target URL under test: UTC Electronic Office portal
            String targetUrl = "https://vanphongdientu.utc.edu.vn/";
            System.out.println("[STEP 2] Navigating to: " + targetUrl);
            driver.get(targetUrl);

            // Inspect and log page metadata to verify successful navigation
            String pageTitle = driver.getTitle();
            String currentUrl = driver.getCurrentUrl();
            System.out.println("[STEP 3] Successfully loaded page.");
            System.out.println("         -> Page Title: " + pageTitle);
            System.out.println("         -> Current URL: " + currentUrl);

            // Pause execution briefly (3 seconds) to allow visual verification of the loaded page
            System.out.println("[STEP 4] Pausing for 3 seconds for visual inspection...");
            Thread.sleep(3000);

        } catch (InterruptedException e) {
            System.err.println("[ERROR] Thread sleep interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error during browser automation: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up: Always quit the driver to terminate the browser process and free system resources
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
