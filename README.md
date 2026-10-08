# UTC Electronic Office - Kiểm Thử Giao Diện Web Tự Động

Dự án kiểm thử tự động giao diện web (Web UI Testing) cho hệ thống Văn phòng điện tử UTC (https://vanphongdientu.utc.edu.vn/Login).
Dự án được xây dựng theo mô hình Page Object Model (POM) và kiểm thử hướng dữ liệu (Data-Driven Testing) sử dụng Java, Selenium WebDriver, JUnit 5 và Allure Report.

## 1. Công nghệ sử dụng

- Ngôn ngữ: Java (JDK 17 trở lên)
- Công cụ kiểm thử: Selenium WebDriver 4.29.0
- Framework kiểm thử: JUnit 5 (Jupiter)
- Báo cáo kiểm thử trực quan: Allure Report 2.27.0
- Thư viện Assertion: AssertJ 3.25.3
- Thư viện xử lý Excel: Apache POI 5.3.0
- Công cụ build: Apache Maven (đã tích hợp sẵn Maven Wrapper `mvnw` / `mvnw.cmd`)

## 2. Cấu trúc thư mục

```text
.
|-- mvnw / mvnw.cmd                          # Maven Wrapper chạy trực tiếp không cần cài trước Maven
|-- pom.xml                                  # File cấu hình thư viện và build Maven (kèm Allure Plugin)
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
                |   `-- ScreenshotWatcher.java # JUnit 5 Extension: đính kèm screenshot vào Allure và lưu cục bộ
                |-- demo/
                |   `-- BrowserLaunchDemoTest.java # Test kiểm tra khởi tạo và đóng trình duyệt
                |-- pages/
                |   |-- LoginPage.java       # Page Object cho trang Đăng nhập
                |   `-- HomePage.java        # Page Object cho trang Chủ sau khi đăng nhập
                |-- utils/
                |   `-- ExcelUtils.java      # Tiện ích đọc và ghi dữ liệu file Excel
                `-- tests/
                    `-- LoginE2ETest.java    # Bộ kiểm thử JUnit 5 chính, gắn nhãn Allure và DDT từ Excel
```

## 3. Yêu cầu môi trường

Để chạy dự án trên bất kỳ máy tính nào:
1. Java Development Kit (JDK): Phiên bản 17 trở lên (đã cấu hình biến môi trường `JAVA_HOME`).
2. Google Chrome: Phiên bản mới nhất (Selenium Manager sẽ tự động tải và cấu hình ChromeDriver tương thích).

Lưu ý: Dự án đã tích hợp sẵn **Maven Wrapper (`mvnw`)**, do đó bạn **không cần cài đặt trước Maven** hay cấu hình biến môi trường `PATH` cho Maven trên máy.

## 4. Hướng dẫn chạy kiểm thử và xem báo cáo Allure

Mở Terminal hoặc Command Prompt tại thư mục gốc của dự án và sử dụng các câu lệnh với bộ chạy tích hợp `.\mvnw`:

### Chạy toàn bộ các ca kiểm thử
```powershell
.\mvnw test
```

### Chạy bộ kiểm thử đăng nhập chính (hiển thị trình duyệt Chrome)
```powershell
.\mvnw test -Dtest=LoginE2ETest
```

### Chạy ở chế độ Headless (chạy ngầm, không mở cửa sổ trình duyệt)
```powershell
.\mvnw test -Dtest=LoginE2ETest -Dheadless=true
```

### Chạy test demo khởi tạo trình duyệt (Smoke test)
```powershell
.\mvnw test -Dtest=BrowserLaunchDemoTest
```

### Xem báo cáo Allure Report trực quan trên trình duyệt
Sau khi chạy kiểm thử xong, chạy lệnh sau để mở giao diện báo cáo:
```powershell
.\mvnw allure:serve
```
Trình duyệt sẽ tự động mở trang Dashboard Allure hiển thị biểu đồ tỷ lệ đạt/trượt, thời gian chạy và ảnh chụp màn hình khi có lỗi.
Nhấn `Ctrl + C` tại cửa sổ Terminal để dừng server khi xem xong.

### Tạo file báo cáo tĩnh HTML (Offline Report)
Nếu cần xuất báo cáo tĩnh HTML để lưu trữ hoặc gửi báo cáo:
```powershell
.\mvnw allure:report
```
Báo cáo HTML sẽ được lưu tại thư mục: `target/site/allure-maven-plugin/`.

### Chạy kiểm thử với tài khoản thật (tùy chọn)
Nếu muốn kiểm thử kịch bản đăng nhập thành công vào trang chủ bằng tài khoản thực tế, truyền thông tin tài khoản qua tham số dòng lệnh:
```powershell
.\mvnw test -Dtest=LoginE2ETest -DUTC_USER="ten_dang_nhap" -DUTC_PASS="mat_khau"
```
*(Nếu không truyền tài khoản thật, ca kiểm thử happy path sẽ tự động được bỏ qua - skip để tránh thất bại vì thiếu thông tin xác thực).*

*(Lưu ý: Đối với hệ điều hành macOS hoặc Linux, thay `.\mvnw` bằng `./mvnw`)*.

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

Nếu có bất kỳ ca kiểm thử nào thất bại (FAIL), lớp `ScreenshotWatcher` sẽ:
1. Tự động đính kèm ảnh chụp màn hình trực tiếp vào báo cáo **Allure Report** tương ứng với ca kiểm thử bị lỗi.
2. Đồng thời lưu một bản ảnh chụp màn hình vào thư mục:
```text
target/screenshots/
```
