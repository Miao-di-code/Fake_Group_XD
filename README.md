# Online Auction System

Ứng dụng đấu giá trực tuyến Java 17 gồm JavaFX/FXML client, TCP JSON server và SQLite. Maven có ba module: `common` (model/DTO/protocol), `server` (service/DAO/socket), `client` (GUI/socket). Chỉ server truy cập database. **Chỉ muốn sử dụng ứng dụng:** xem [hướng dẫn người dùng](docs/hdsd.md). **Muốn học dự án:** bắt đầu từ [docs/guide.md](docs/guide.md).

## 1. Chuẩn bị Windows và VS Code

Mở thư mục `D:\BTL_LTNC` bằng VS Code (File → Open Folder). Cài JDK 17 và Maven 3.9.x, hoặc dùng môi trường Anaconda `_env` đã chuẩn bị trong thư mục này. `environment.yml` ghi phiên bản OpenJDK/Maven. Nếu có Conda nhưng chưa có `_env`, tạo bằng `conda env create -p .\_env -f environment.yml`; Maven cần tải dependency lần đầu khi có mạng. JavaFX cần Windows desktop.

Các script PowerShell dưới đây chọn JDK/Maven và repository từ `_env`; không cần tự đặt CLASSPATH hay tìm Main Class. Trong PowerShell ở root:

```powershell
.\scripts\Use-LocalConda.ps1
java -version
mvn -version
.\scripts\run-tests.ps1
```

Nếu dùng JDK/Maven hệ thống thay cho `_env`, chạy trực tiếp `mvn -B clean verify` khi dependency có thể tải từ Maven Central. Bản CI cũng dùng lệnh này. Tại môi trường cục bộ hiện tại, `run-tests.ps1` dùng `mvn -o -B clean verify` với dependency đã lưu trong `_env`; `-Online` bỏ cờ offline.

## 2. Database và dữ liệu demo

`DatabaseInitializer` tự tạo/nâng schema SQLite khi seed hoặc khởi động server. Không cần chạy SQL thủ công. Seed một database demo riêng:

```powershell
.\scripts\seed-demo.ps1
```

Mặc định script ghi `data/demo.db`, tạo một Admin, một Seller, hai Bidder; ba sản phẩm Electronics/Art/Vehicle; một phiên RUNNING và hai phiên OPEN. Chạy lại không tạo trùng. Phiên RUNNING có hạn khoảng 30 phút từ lần seed đầu; nếu đã kết thúc, Seller có thể tạo sản phẩm/phiên mới. Mật khẩu chỉ dành cho demo local, lưu dạng hash trong SQLite. Nếu cần đổi trước lần seed đầu, đặt biến môi trường `AUCTION_DEMO_PASSWORD` trong PowerShell; seed lại với mật khẩu khác trên cùng database sẽ bị từ chối.

| Vai trò | Username | Mật khẩu local mặc định |
| --- | --- | --- |
| Admin | `demo_admin` | `DemoPass123!` |
| Seller | `demo_seller` | `DemoPass123!` |
| Bidder A | `demo_bidder_a` | `DemoPass123!` |
| Bidder B | `demo_bidder_b` | `DemoPass123!` |

`data/` nằm trong `.gitignore`; không đưa database demo hay password thật vào Git. Admin không được đăng ký qua GUI; ngoài demo, dùng `AdminBootstrap` theo [BACKEND_GUIDE.md](docs/BACKEND_GUIDE.md).

## 3. Chạy Server và nhiều Client

Mở PowerShell thứ nhất tại `D:\BTL_LTNC`:

```powershell
.\scripts\run-server.ps1 -DatabasePath 'data/demo.db'
```

Server bind `127.0.0.1:5555` mặc định. `-Port`, `-DatabasePath`, `-AntiSnipeWindowSeconds`, `-AntiSnipeExtensionSeconds` là tham số tùy chọn; mặc định chống bid sát giờ là 30/30 giây. Ctrl+C dừng server. Trong PowerShell thứ hai và thứ ba, mỗi cửa sổ chạy:

```powershell
.\scripts\run-client.ps1
```

`ClientMain` mở cửa sổ login bằng `javafx-maven-plugin`; mỗi lần chạy là một client riêng. Đăng nhập Seller tạo sản phẩm và phiên, mở hai client Bidder xem cùng phiên rồi đặt giá; giá và trạng thái được server đẩy tới cửa sổ đang xem. Màn chi tiết có lịch sử/biểu đồ, auto-bid và countdown. `scripts/run-demo.ps1` gộp seed và mở server ở cửa sổ hiện tại; sau đó mở client ở cửa sổ khác. Hướng dẫn click chi tiết ở [DEMO_SCENARIOS.md](docs/DEMO_SCENARIOS.md) và [GUI_GUIDE.md](docs/GUI_GUIDE.md).

## 4. Test và báo cáo

```powershell
.\scripts\run-tests.ps1
```

Script chạy Maven `clean verify`: JUnit 5, Checkstyle, JaCoCo (server phải đạt line coverage ≥60%). Test dùng database tạm, không sửa `data/demo.db`. Kết quả XML/HTML ở `*/target/surefire-reports` và `*/target/site/jacoco`. E2E TCP với server chạy đúng cổng 5555 có thể chạy riêng khi cổng đang trống:

```powershell
.\scripts\Use-LocalConda.ps1
mvn -o -B -pl server -am test '-Dtest=SocketServerIntegrationTest#finalEndToEndFlowSurvivesServerRestartAndKeepsWinner' '-Dsurefire.failIfNoSpecifiedTests=false' '-Dauction.e2e.port=5555'
```

GUI smoke test `scripts/test-gui.ps1` cần server thật đang chạy với DB tạm; với DB đã seed, `scripts/test-gui.ps1 -Admin` kiểm tra login/Admin/logout. Hướng dẫn ở [GUI_GUIDE.md](docs/GUI_GUIDE.md). Kết quả nghiệm thu và giới hạn ở [FINAL_ACCEPTANCE_REPORT.md](docs/FINAL_ACCEPTANCE_REPORT.md). Workflow CI ở `.github/workflows/ci.yml` chạy `mvn -B clean verify` trên JDK 17; chưa khẳng định GitHub Actions xanh nếu chưa chạy trên GitHub.

## 5. Tài liệu và cấu trúc

[docs/guide.md](docs/guide.md) là mục lục đầy đủ theo thứ tự học. [ARCHITECTURE.md](docs/ARCHITECTURE.md) có sơ đồ lớp, OOP, mẫu thiết kế và bản đồ module; [CODE_WALKTHROUGH.md](docs/CODE_WALKTHROUGH.md) lần theo một lượt đặt giá. [REQUIREMENTS.md](docs/REQUIREMENTS.md) đối chiếu hai PDF; [CONTRACTS.md](docs/CONTRACTS.md) và [API_PROTOCOL.md](docs/API_PROTOCOL.md) ghi giao thức; [FINAL_ACCEPTANCE_REPORT.md](docs/FINAL_ACCEPTANCE_REPORT.md) ghi kết quả lịch sử. Kế hoạch commit là dự kiến, không phải lịch sử Git.
