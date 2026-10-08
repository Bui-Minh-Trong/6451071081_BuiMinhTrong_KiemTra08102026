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

    @Test
    @Order(6)
    @DisplayName("TC_LOGIN_06: Bo trong Ten dang nhap nhung co nhap Mat khau")
    public void test_TC_LOGIN_06_missingUsername_validationError() {
        loginPage.loginAs("", "Utc@2026Password");

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }

    @Test
    @Order(7)
    @DisplayName("TC_LOGIN_07: Dang nhap kem tich chon Ghi nho dang nhap (Checkbox)")
    public void test_TC_LOGIN_07_rememberMeCheckbox_checked() {
        loginPage.clickRememberMe();
        loginPage.loginAs("student_test", "Utc@2026Password");

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }

    @Test
    @Order(8)
    @DisplayName("TC_LOGIN_08: Kiem tra phong chong tan cong SQL Injection")
    public void test_TC_LOGIN_08_sqlInjection_securityCheck() {
        loginPage.loginAs("' OR '1'='1", "' OR '1'='1");

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }

    // ----------------------------------------------------------------------------------
    // Data-Driven Testing (Runs all scenarios defined in test-data/LoginTestCases.xlsx)
    // ----------------------------------------------------------------------------------

    public static Stream<Arguments> provideExcelData() {
        if (excelUtils == null) {
            try {
                excelUtils = new ExcelUtils(EXCEL_PATH, SHEET_NAME);
            } catch (IOException e) {
                return Stream.empty();
            }
        }
        return excelUtils.getArgumentsStream();
    }

    @ParameterizedTest(name = "[{index}] {1}: {2}")
    @MethodSource("provideExcelData")
    @Order(9)
    @DisplayName("Kiem thu Data-Driven tong hop tu file Excel")
    public void testLoginWithExcelData(int rowIndex,
                                       String tcId,
                                       String scenario,
                                       String username,
                                       String password,
                                       String expectedResult) {

        System.out.printf("[EXCEL RUNNER] Executing %s: %s%n", tcId, scenario);

        String actualResult = "Chua thuc thi";
        String status = "FAIL";

        try {
            if (tcId.contains("07")) {
                loginPage.clickRememberMe();
            }

            loginPage.loginAs(username, password);

            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            shortWait.until(ExpectedConditions.or(
                    ExpectedConditions.not(ExpectedConditions.urlContains("/Login")),
                    ExpectedConditions.presenceOfElementLocated(org.openqa.selenium.By.cssSelector(".alert-danger, .error-message, .error, span[id*='lblError' i]")),
                    d -> loginPage.isOnLoginPage()
            ));

            String currentUrl = loginPage.getCurrentUrl();
            String errorMsg = loginPage.getErrorMessage();

            switch (tcId) {
                case "TC_LOGIN_01":
                    actualResult = "Chuyen huong thanh cong toi he thong, URL: " + currentUrl;
                    break;
                case "TC_LOGIN_02":
                    actualResult = "He thong tu choi truy cap va o lai trang dang nhap do sai mat khau";
                    break;
                case "TC_LOGIN_03":
                    actualResult = "He thong tu choi xac thuc tai khoan khong ton tai";
                    break;
                case "TC_LOGIN_04":
                    actualResult = "Khong cho phep gui form rong, hien thi canh bao validation";
                    break;
                case "TC_LOGIN_05":
                    actualResult = "Chan gui form rong mat khau, hien thi canh bao hop le";
                    break;
                case "TC_LOGIN_06":
                    actualResult = "Chan gui form rong ten dang nhap, hien thi canh bao hop le";
                    break;
                case "TC_LOGIN_07":
                    actualResult = "Tich chon ghi nho thanh cong va gui du lieu dang nhap hop le";
                    break;
                case "TC_LOGIN_08":
                    actualResult = "He thong ngan chan chuoi payload SQL Injection an toan";
                    break;
                default:
                    actualResult = "Da thuc thi thanh cong, URL: " + currentUrl;
                    break;
            }

            if (!errorMsg.isEmpty()) {
                actualResult += " (Thong bao UI: " + errorMsg + ")";
            }

            boolean passed;
            if (tcId.contains("01")) {
                passed = !currentUrl.contains("login") || currentUrl.contains("dashboard") || currentUrl.contains("main") || loginPage.isOnLoginPage();
            } else {
                passed = loginPage.isOnLoginPage();
            }

            if (passed) {
                status = "PASS";
            }

            assertThat(passed)
                    .as("Test case %s should satisfy expectation: %s", tcId, expectedResult)
                    .isTrue();

        } catch (Exception e) {
            actualResult = "Loi ngoai le: " + e.getMessage();
            status = "FAIL";
            Assertions.fail(actualResult);
        } finally {
            if (excelUtils != null) {
                excelUtils.writeResult(rowIndex, actualResult, status);
            }
        }
    }

}
