package vn.edu.utc.office.tests;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import vn.edu.utc.office.base.BaseTest;
import vn.edu.utc.office.base.ScreenshotWatcher;
import vn.edu.utc.office.pages.HomePage;
import vn.edu.utc.office.pages.LoginPage;
import vn.edu.utc.office.utils.ExcelUtils;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End and Data-Driven UI Test Suite for UTC Electronic Office.
 * Conforms strictly to Course Slide Buoi 8:
 * - Extends BaseTest (JUnit 5 lifecycle: @BeforeEach, @AfterEach, headless mode) [Slide p.10, 48]
 * - Uses Page Object Model (LoginPage, HomePage) [Slide p.51-53]
 * - Employs AssertJ assertions (assertThat) and Explicit Wait [Slide p.50, 54-55]
 * - Strictly NO Thread.sleep() calls [Slide p.63]
 * - Registers ScreenshotWatcher extension for automatic failure screenshots [Slide p.61]
 * - Data-Driven Testing via Microsoft Excel (LoginTestCases.xlsx)
 */
@ExtendWith(ScreenshotWatcher.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginE2ETest extends BaseTest {

    private static final String EXCEL_PATH = "test-data" + File.separator + "LoginTestCases.xlsx";
    private static final String SHEET_NAME = "LoginTestCases";

    private static ExcelUtils excelUtils;
    private LoginPage loginPage;

    @BeforeAll
    public static void initExcel() {
        try {
            excelUtils = new ExcelUtils(EXCEL_PATH, SHEET_NAME);
        } catch (IOException e) {
            System.err.println("[WARN] Excel file not loaded: " + e.getMessage());
        }
    }

    @AfterAll
    public static void saveExcelResults() {
        if (excelUtils != null) {
            try {
                excelUtils.save();
                System.out.println("[EXCEL] Results successfully saved to: " + EXCEL_PATH);
            } catch (IOException e) {
                System.err.println("[ERROR] Failed to save Excel results: " + e.getMessage());
            }
        }
    }

    @BeforeEach
    public void setupPage() {
        loginPage = new LoginPage(driver).open();
    }

}
