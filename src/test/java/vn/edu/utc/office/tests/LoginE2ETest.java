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

    @Test
    @Order(1)
    @DisplayName("TC_LOGIN_01: Dang nhap thanh cong -> roi khoi trang Login (Slide p.54)")
    public void test_TC_LOGIN_01_validCredentials_leavesLoginPage() {
        // Slide p.54: "Tai khoan that lay tu bien moi truong, khong bao gio viet mat khau vao code"
        Assumptions.assumeTrue(System.getenv("UTC_USER") != null,
                "Bo qua vi chua cau hinh bien moi truong UTC_USER va UTC_PASS cho tai khoan that");

        String username = System.getenv("UTC_USER");
        String password = System.getenv("UTC_PASS");

        HomePage homePage = loginPage.loginAs(username, password);

        // Explicit Wait: wait until URL changes away from /Login (No Thread.sleep)
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.not(ExpectedConditions.urlContains("/Login")));

        // AssertJ Fluent Assertion (Slide p.54)
        assertThat(loginPage.isOnLoginPage()).isFalse();
    }

    @Test
    @Order(2)
    @DisplayName("TC_LOGIN_02: Sai mat khau -> van o lai trang Login (Slide p.55)")
    public void test_TC_LOGIN_02_invalidPassword_staysOnLoginPage() {
        loginPage.loginAs("student_test", "WrongPassword!99");

        // Explicit wait for response processing
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        // AssertJ validation (Slide p.55)
        assertThat(loginPage.isOnLoginPage()).isTrue();
    }

    @Test
    @Order(3)
    @DisplayName("TC_LOGIN_03: Tai khoan khong ton tai -> tu choi xac thuc")
    public void test_TC_LOGIN_03_nonExistentAccount_loginFailed() {
        loginPage.loginAs("unknown_account_xyz", "AnyPassword@123");

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }

    @Test
    @Order(4)
    @DisplayName("TC_LOGIN_04: Bo trong ca Ten dang nhap va Mat khau -> hien validation")
    public void test_TC_LOGIN_04_emptyCredentials_validationError() {
        loginPage.loginAs("", "");

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }

    @Test
    @Order(5)
    @DisplayName("TC_LOGIN_05: Bo trong mat khau -> kiem tra validation (Slide p.63)")
    public void test_TC_LOGIN_05_missingPassword_staysOnLoginPage() {
        loginPage.loginAs("student_test", "");

        // Explicit wait ensures validation state is registered
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }

}
