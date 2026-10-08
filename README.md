# UTC Electronic Office - Kiểm Thử Giao Diện Web Tự Động

Dự án kiểm thử tự động giao diện web (Web UI Testing) cho hệ thống Văn phòng điện tử UTC (https://vanphongdientu.utc.edu.vn/Login).
Dự án được xây dựng theo mô hình Page Object Model (POM) và kiểm thử hướng dữ liệu (Data-Driven Testing) sử dụng Java, Selenium WebDriver và JUnit 5.

## 1. Công nghệ sử dụng

- Ngôn ngữ: Java (JDK 17 trở lên)
- Công cụ kiểm thử: Selenium WebDriver 4.29.0
- Framework kiểm thử: JUnit 5 (Jupiter)
- Thư viện Assertion: AssertJ 3.25.3
- Thư viện xử lý Excel: Apache POI 5.3.0
- Công cụ build và quản lý thư viện: Apache Maven 3.8+

## 2. Cấu trúc thư mục

```text
.
|-- pom.xml                                  # File cấu hình thư viện và build Maven
|-- README.md                                # Hướng dẫn cài đặt và sử dụng
|-- test-data/
|   `-- LoginTestCases.xlsx                  # File Excel chứa bộ dữ liệu kiểm thử và kết quả
`-- src/
    `-- test/
        `-- java/
            `-- vn/edu/utc/office/
                |-- base/
                |   |-- BasePage.java        # Lớp cơ sở trang: triển khai Explicit Wait (click, type, getText)
                |   |-- BaseTest.java        # Lớp cơ sở test: quản lý vòng đời WebDriver và chế độ headless
                |   `-- ScreenshotWatcher.java # JUnit 5 Extension: tự động chụp ảnh màn hình khi test thất bại
                |-- demo/
                |   `-- BrowserLaunchDemoTest.java # Test kiểm tra khởi tạo và đóng trình duyệt
                |-- pages/
                |   |-- LoginPage.java       # Page Object cho trang Đăng nhập
                |   `-- HomePage.java        # Page Object cho trang Chủ sau khi đăng nhập
                |-- utils/
                |   `-- ExcelUtils.java      # Tiện ích đọc và ghi dữ liệu file Excel
                `-- tests/
                    `-- LoginE2ETest.java    # Bộ kiểm thử JUnit 5 chính và Data-Driven từ file Excel
```

## 3. Yêu cầu môi trường

Để chạy được dự án trên bất kỳ máy tính nào (Windows, macOS, Linux), cần chuẩn bị:

1. Java Development Kit (JDK): Phiên bản 17 trở lên (đã cấu hình biến môi trường `JAVA_HOME`).
2. Apache Maven: Phiên bản 3.8 trở lên (đã thêm thư mục `bin` vào biến môi trường `PATH`).
3. Google Chrome: Phiên bản mới nhất (Selenium Manager sẽ tự động tải và cấu hình ChromeDriver tương thích).

Kiểm tra môi trường trong Terminal / Command Prompt:
```bash
java -version
mvn -version
```

## 4. Hướng dẫn chạy kiểm thử

Mở Terminal hoặc Command Prompt tại thư mục gốc của dự án và sử dụng các câu lệnh tiêu chuẩn sau:

### Chạy toàn bộ các ca kiểm thử
```bash
mvn test
```

### Chạy bộ kiểm thử đăng nhập chính (hiển thị trình duyệt Chrome)
```bash
mvn test -Dtest=LoginE2ETest
```

### Chạy ở chế độ Headless (chạy ngầm, không mở cửa sổ trình duyệt)
```bash
mvn test -Dtest=LoginE2ETest -Dheadless=true
```

### Chạy test demo khởi tạo trình duyệt (Smoke test)
```bash
mvn test -Dtest=BrowserLaunchDemoTest
```

### Chạy kiểm thử với tài khoản thật (tùy chọn)
Nếu muốn kiểm thử kịch bản đăng nhập thành công vào trang chủ bằng tài khoản thực tế, truyền thông tin tài khoản qua tham số dòng lệnh:
```bash
mvn test -Dtest=LoginE2ETest -DUTC_USER="ten_dang_nhap" -DUTC_PASS="mat_khau"
```
*(Nếu không truyền tài khoản thật, ca kiểm thử happy path sẽ tự động được bỏ qua - skip để tránh thất bại vì thiếu thông tin xác thực).*

## 5. Danh sách ca kiểm thử trong file Excel

Dữ liệu kiểm thử được quản lý tại file `test-data/LoginTestCases.xlsx` bao gồm 8 kịch bản:

- TC_LOGIN_01: Đăng nhập thành công với thông tin tài khoản hợp lệ (Happy Path)
- TC_LOGIN_02: Đăng nhập thất bại do nhập sai mật khẩu (Negative Test)
- TC_LOGIN_03: Đăng nhập thất bại do tài khoản không tồn tại (Negative Test)
- TC_LOGIN_04: Bỏ trống cả Tên đăng nhập và Mật khẩu (Validation Test)
- TC_LOGIN_05: Nhập Tên đăng nhập nhưng bỏ trống Mật khẩu (Validation Test)
- TC_LOGIN_06: Bỏ trống Tên đăng nhập nhưng có nhập Mật khẩu (Validation Test)
- TC_LOGIN_07: Đăng nhập kèm tích chọn checkbox Ghi nhớ đăng nhập
- TC_LOGIN_08: Kiểm tra phòng chống tấn công SQL Injection

Lưu ý: Sau mỗi lần chạy kiểm thử, kết quả thực tế (Actual Result) và trạng thái (PASS/FAIL) sẽ được hệ thống tự động ghi nhận và cập nhật trực tiếp vào file Excel trên.

## 6. Cơ chế chụp ảnh màn hình khi có lỗi

Nếu có bất kỳ ca kiểm thử nào thất bại (FAIL), lớp `ScreenshotWatcher` sẽ tự động chụp lại ảnh màn hình tại thời điểm phát sinh lỗi và lưu vào thư mục:

```text
target/screenshots/
```
