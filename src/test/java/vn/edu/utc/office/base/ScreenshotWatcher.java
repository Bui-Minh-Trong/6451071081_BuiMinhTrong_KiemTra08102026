package vn.edu.utc.office.base;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JUnit 5 Extension: Automatically captures and stores screenshots upon test failure.
 * Conforms to Course Slide Buoi 8 (page 61) and attaches screenshots directly into Allure Report.
 */
public class ScreenshotWatcher implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext ctx) throws Exception {
        if (ctx.getExecutionException().isPresent()) { // Triggered when a test FAILS
            WebDriver driver = BaseTest.getDriver();
            if (driver instanceof TakesScreenshot) {
                byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

                // 1. Attach failure screenshot into Allure Report
                try {
                    Allure.addAttachment(
                            "Failure Screenshot - " + ctx.getDisplayName(),
                            "image/png",
                            new ByteArrayInputStream(screenshotBytes),
                            ".png"
                    );
                } catch (Exception ignored) {
                }

                // 2. Also save locally to target/screenshots
                Path screenshotDir = Path.of("target/screenshots");
                Files.createDirectories(screenshotDir);

                String testName = ctx.getDisplayName().replaceAll("[^a-zA-Z0-9_.-]", "_");
                Path dest = screenshotDir.resolve(testName + ".png");
                Files.write(dest, screenshotBytes);
                System.out.println("[SCREENSHOT] Failure captured -> " + dest.toAbsolutePath());
            }
        }
    }
}
