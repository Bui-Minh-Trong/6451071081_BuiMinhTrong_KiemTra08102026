package vn.edu.utc.office.base;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * JUnit 5 Extension: Automatically captures and stores screenshots upon test failure.
 * Conforms to Slide Buoi 8 (page 61).
 */
public class ScreenshotWatcher implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext ctx) throws Exception {
        if (ctx.getExecutionException().isPresent()) { // Triggered when a test FAILS
            WebDriver driver = BaseTest.getDriver();
            if (driver instanceof TakesScreenshot) {
                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Path screenshotDir = Path.of("target/screenshots");
                Files.createDirectories(screenshotDir);

                String testName = ctx.getDisplayName().replaceAll("[^a-zA-Z0-9_.-]", "_");
                Path dest = screenshotDir.resolve(testName + ".png");
                Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[SCREENSHOT] Failure captured -> " + dest.toAbsolutePath());
            }
        }
    }
}
