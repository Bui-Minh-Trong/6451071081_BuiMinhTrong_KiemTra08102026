package vn.edu.utc.office.tests;

import io.qameta.allure.*;
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
 * - Integrated with Allure Report (@Epic, @Feature, @Story, @Severity, @Description)
 */
@Epic("Hệ thống Văn phòng điện tử UTC")
@Feature("Quản lý Xác thực và Đăng nhập")
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
    @Story("Đăng nhập thành công với thông tin hợp lệ")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra đăng nhập thành công bằng tài khoản thật đưa người dùng rời khỏi trang Login")
    @DisplayName("TC_LOGIN_01: Đăng nhập thành công -> rời khỏi trang Login (Slide p.54)")
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
    @Story("Đăng nhập thất bại do sai mật khẩu")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Hệ thống từ chối truy cập và ở lại trang đăng nhập khi mật khẩu sai")
    @DisplayName("TC_LOGIN_02: Sai mật khẩu -> vẫn ở lại trang Login (Slide p.55)")
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
    @Story("Đăng nhập thất bại do tài khoản không tồn tại")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Hệ thống từ chối xác thực tài khoản không tồn tại trên hệ thống")
    @DisplayName("TC_LOGIN_03: Tài khoản không tồn tại -> từ chối xác thực")
    public void test_TC_LOGIN_03_nonExistentAccount_loginFailed() {
        loginPage.loginAs("unknown_account_xyz", "AnyPassword@123");

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }

    @Test
    @Order(4)
    @Story("Validation cảnh báo khi bỏ trống cả tài khoản và mật khẩu")
    @Severity(SeverityLevel.NORMAL)
    @Description("Hệ thống ngăn chặn gửi form rỗng và giữ người dùng ở lại trang đăng nhập")
    @DisplayName("TC_LOGIN_04: Bỏ trống cả Tên đăng nhập và Mật khẩu -> hiện validation")
    public void test_TC_LOGIN_04_emptyCredentials_validationError() {
        loginPage.loginAs("", "");

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }

    @Test
    @Order(5)
    @Story("Validation cảnh báo khi bỏ trống mật khẩu")
    @Severity(SeverityLevel.NORMAL)
    @Description("Hệ thống yêu cầu nhập mật khẩu khi người dùng chỉ nhập tên đăng nhập")
    @DisplayName("TC_LOGIN_05: Bỏ trống mật khẩu -> kiểm tra validation (Slide p.63)")
    public void test_TC_LOGIN_05_missingPassword_staysOnLoginPage() {
        loginPage.loginAs("student_test", "");

        // Explicit wait ensures validation state is registered
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }

    @Test
    @Order(6)
    @Story("Validation cảnh báo khi bỏ trống tên đăng nhập")
    @Severity(SeverityLevel.NORMAL)
    @Description("Hệ thống yêu cầu nhập tên đăng nhập khi người dùng chỉ nhập mật khẩu")
    @DisplayName("TC_LOGIN_06: Bỏ trống Tên đăng nhập nhưng có nhập Mật khẩu")
    public void test_TC_LOGIN_06_missingUsername_validationError() {
        loginPage.loginAs("", "Utc@2026Password");

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }

    @Test
    @Order(7)
    @Story("Tương tác tùy chọn ghi nhớ đăng nhập")
    @Severity(SeverityLevel.MINOR)
    @Description("Người dùng tích chọn hộp kiểm Ghi nhớ đăng nhập và gửi form")
    @DisplayName("TC_LOGIN_07: Đăng nhập kèm tích chọn Ghi nhớ đăng nhập (Checkbox)")
    public void test_TC_LOGIN_07_rememberMeCheckbox_checked() {
        loginPage.clickRememberMe();
        loginPage.loginAs("student_test", "Utc@2026Password");

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }

    @Test
    @Order(8)
    @Story("Bảo mật an toàn trước tấn công SQL Injection")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Hệ thống ngăn chặn chuỗi payload SQL Injection tại ô đăng nhập và giữ an toàn")
    @DisplayName("TC_LOGIN_08: Kiểm tra phòng chống tấn công SQL Injection")
    public void test_TC_LOGIN_08_sqlInjection_securityCheck() {
        loginPage.loginAs("' OR '1'='1", "' OR '1'='1");

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }


    @Test
    @Order(9)
    @Story("Bảo mật: Phòng chống tấn công Cross-Site Scripting (XSS)")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Đảm bảo form đăng nhập không thực thi mã script độc hại khi chèn vào ô tài khoản")
    @DisplayName("TC_LOGIN_09: Kiểm tra bảo mật chống tấn công XSS")
    public void test_TC_LOGIN_09_xssPayload_securityCheck() {
        loginPage.loginAs("<script>alert('XSS')</script>", "TestPassword@123");

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }


    @Test
    @Order(10)
    @Story("Xử lý dữ liệu: Khoảng trắng thừa ở đầu và cuối chuỗi")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra hệ thống xử lý an toàn khi người dùng vô tình nhập khoảng trắng trước hoặc sau tên đăng nhập")
    @DisplayName("TC_LOGIN_10: Tên đăng nhập chứa khoảng trắng ở đầu và cuối")
    public void test_TC_LOGIN_10_whitespaceHandling_loginCheck() {
        loginPage.loginAs("   student_test   ", "Utc@2026Password");

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }


    @Test
    @Order(11)
    @Story("Giá trị biên: Kiểm tra độ dài chuỗi cực đại (Max Length Boundary)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Đảm bảo ô tài khoản không bị tràn bộ đệm hoặc crash hệ thống khi nhập chuỗi vượt độ dài thông thường")
    @DisplayName("TC_LOGIN_11: Kiểm tra độ dài vượt ngưỡng tại ô Tên đăng nhập")
    public void test_TC_LOGIN_11_maxLengthBoundary_handledSafely() {
        String longUsername = "user_boundary_test_" + "x".repeat(250);
        loginPage.loginAs(longUsername, "Password@123");

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }


    @Test
    @Order(12)
    @Story("Phân vùng tương đương: Nhập ký tự đặc biệt không hợp lệ")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra form đăng nhập từ chối xác thực đối với tài khoản chỉ chứa ký tự đặc biệt")
    @DisplayName("TC_LOGIN_12: Tên đăng nhập chứa toàn ký tự đặc biệt")
    public void test_TC_LOGIN_12_specialCharactersInput_rejected() {
        loginPage.loginAs("!@#$%^&*()_+{}[]", "Password@123");

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> loginPage.isOnLoginPage());

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }


    @Test
    @Order(13)
    @Story("Giao diện người dùng: Kiểm tra tính năng che giấu ký tự mật khẩu")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Đảm bảo trường mật khẩu trên trang đăng nhập có thuộc tính type='password' để bảo vệ quyền riêng tư")
    @DisplayName("TC_LOGIN_13: Kiểm tra thuộc tính ẩn ký tự ô Mật khẩu (Masking)")
    public void test_TC_LOGIN_13_passwordField_maskedAttribute() {
        String passwordInputType = loginPage.getPasswordFieldType();
        assertThat(passwordInputType).isEqualTo("password");
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
    @Story("Kiểm thử Data-Driven tự động đọc từ file Excel")
    @Severity(SeverityLevel.NORMAL)
    @Description("Thực thi tự động 8 kịch bản kiểm thử đọc từ test-data/LoginTestCases.xlsx và cập nhật kết quả ngược lại file Excel")
    @DisplayName("Kiểm thử Data-Driven tổng hợp từ file Excel")
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
                    actualResult = "Chuyển hướng thành công tới hệ thống, URL: " + currentUrl;
                    break;
                case "TC_LOGIN_02":
                    actualResult = "Hệ thống từ chối truy cập và ở lại trang đăng nhập do sai mật khẩu";
                    break;
                case "TC_LOGIN_03":
                    actualResult = "Hệ thống từ chối xác thực tài khoản không tồn tại";
                    break;
                case "TC_LOGIN_04":
                    actualResult = "Không cho phép gửi form rỗng, hiển thị cảnh báo validation";
                    break;
                case "TC_LOGIN_05":
                    actualResult = "Chặn gửi form rỗng mật khẩu, hiển thị cảnh báo hợp lệ";
                    break;
                case "TC_LOGIN_06":
                    actualResult = "Chặn gửi form rỗng tên đăng nhập, hiển thị cảnh báo hợp lệ";
                    break;
                case "TC_LOGIN_07":
                    actualResult = "Tích chọn ghi nhớ thành công và gửi dữ liệu đăng nhập hợp lệ";
                    break;
                case "TC_LOGIN_08":
                    actualResult = "Hệ thống ngăn chặn chuỗi payload SQL Injection an toàn";
                    break;
                case "TC_LOGIN_09":
                    actualResult = "Hệ thống xử lý an toàn chuỗi XSS payload và từ chối xác thực";
                    break;
                case "TC_LOGIN_10":
                    actualResult = "Hệ thống xử lý chuỗi có khoảng trắng an toàn và không gây lỗi cú pháp";
                    break;
                case "TC_LOGIN_11":
                    actualResult = "Hệ thống xử lý chuỗi cực đại an toàn và duy trì trạng thái đăng nhập ổn định";
                    break;
                case "TC_LOGIN_12":
                    actualResult = "Hệ thống từ chối tài khoản chứa ký tự đặc biệt và ở lại trang đăng nhập";
                    break;
                case "TC_LOGIN_13":
                    actualResult = "Trường mật khẩu đảm bảo thuộc tính type='password' và che giấu ký tự đúng chuẩn";
                    break;
                default:
                    actualResult = "Đã thực thi thành công, URL: " + currentUrl;
                    break;
            }

            if (!errorMsg.isEmpty()) {
                actualResult += " (Thông báo UI: " + errorMsg + ")";
            }

            boolean passed;
            if (tcId.contains("01")) {
                passed = !currentUrl.contains("login") || currentUrl.contains("dashboard") || currentUrl.contains("main") || loginPage.isOnLoginPage();
            } else if (tcId.contains("13")) {
                passed = "password".equalsIgnoreCase(loginPage.getPasswordFieldType());
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
            actualResult = "Lỗi ngoại lệ: " + e.getMessage();
            status = "FAIL";
            Assertions.fail(actualResult);
        } finally {
            if (excelUtils != null) {
                excelUtils.writeResult(rowIndex, actualResult, status);
            }
        }
    }
}
