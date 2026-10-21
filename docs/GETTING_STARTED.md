# Cài đặt, chạy và thử đấu giá trên Windows

**Mục đích:** Tự mở dự án, chạy một server và nhiều cửa sổ client. Cần Windows có giao diện, VS Code và thư mục dự án `D:\BTL_LTNC`. Nếu chưa biết JDK/Maven, đọc [kiến thức tối thiểu](JAVA_BASICS.md). Các lệnh dưới đây được đối chiếu với `pom.xml` và `scripts/*.ps1` trong dự án.

## 1. Chuẩn bị công cụ

**Mục tiêu:** Có JDK 17, Maven và terminal PowerShell. VS Code chỉ là trình soạn thảo; Java chạy bằng JDK.

**Thao tác:** Cài VS Code bằng bộ cài Windows, mở VS Code, nhấn biểu tượng **Extensions** bên trái (`Ctrl+Shift+X`), tìm **Extension Pack for Java**, chọn **Install**. Có hai cách cung cấp JDK/Maven: dùng `_env` đã có trong dự án, hoặc cài JDK 17 và Maven 3.9.x vào máy. Nếu `_env` chưa có nhưng đã cài Conda và có mạng, mở PowerShell tại `D:\BTL_LTNC` rồi chạy:

```powershell
conda env create -p .\_env -f environment.yml
```

**Kết quả mong đợi:** `_env\Library\bin\java.exe` và `mvn.cmd` tồn tại. **Nếu lỗi:** Kiểm tra `conda --version`, đường dẫn hiện tại bằng `Get-Location`, và kết nối mạng. Nếu dùng JDK/Maven hệ thống, cài bộ JDK 17 cho Windows rồi mở lại PowerShell, kiểm tra `java -version`. Tải bản Maven 3.9.x dạng ZIP, giải nén, thêm thư mục `bin` của Maven vào biến môi trường `Path` trong Windows, mở lại PowerShell rồi kiểm tra `mvn -version`; dòng Java home phải trỏ đến JDK 17. Không chạy `Use-LocalConda.ps1` khi thiếu `_env`.

## 2. Mở dự án và build

**Mục tiêu:** Kiểm tra mã Java, unit test và cấu hình build.

**Thao tác:** Trong VS Code chọn **File → Open Folder → D:\BTL_LTNC**. Chọn **Terminal → New Terminal**, bảo đảm `Get-Location` in ra `D:\BTL_LTNC`. Với `_env` có sẵn, chạy:

```powershell
.\scripts\Use-LocalConda.ps1
java -version
mvn -version
mvn -o -B clean verify
```

Nếu dùng JDK/Maven hệ thống với mạng, chạy `mvn -B clean verify` thay cho lệnh offline. `-o` nghĩa là chỉ dùng thư viện đã tải; lần tải đầu cần mạng. `clean` xóa build cũ; `verify` chạy test, Checkstyle và JaCoCo.

**Kết quả mong đợi:** Maven in `BUILD SUCCESS`; kết quả test nằm trong `*/target/surefire-reports`. **Nếu lỗi:** “Conda environment is missing” nghĩa là `_env` chưa sẵn sàng. “Missing artifact” khi chạy `-o` nghĩa là thiếu dependency trong cache; thử lệnh không có `-o` khi có mạng. Kiểm tra JDK là 17 và đọc dòng `[ERROR]` đầu tiên. Báo cáo test lịch sử nằm ở [nghiệm thu](FINAL_ACCEPTANCE_REPORT.md); lệnh vừa chạy mới là kết quả trên máy của bạn.

## 3. Tạo dữ liệu demo

**Mục tiêu:** Có tài khoản và phiên mẫu để thử giao diện.

**Thao tác:** Tại root dự án, chạy:

```powershell
.\scripts\seed-demo.ps1
```

**Kết quả mong đợi:** Script tạo `data/demo.db`, bốn tài khoản `demo_admin`, `demo_seller`, `demo_bidder_a`, `demo_bidder_b`; mật khẩu local mặc định `DemoPass123!`. Có ba sản phẩm và ba phiên. Chạy lại không nhân đôi dữ liệu. Phiên RUNNING ban đầu hết hạn khoảng 30 phút sau lần seed đầu; Seller có thể tạo phiên mới. **Nếu lỗi:** Kiểm tra `_env`, Maven offline và quyền ghi thư mục `data`. Đừng xóa `data/auction.db` nếu đó là dữ liệu đang dùng. Muốn đổi mật khẩu demo, đặt `AUCTION_DEMO_PASSWORD` trước lần seed đầu; seed lại DB cũ với mật khẩu khác sẽ bị từ chối.

## 4. Chạy server

**Mục tiêu:** Mở chương trình nhận và xử lý yêu cầu đấu giá.

**Thao tác:** Mở terminal PowerShell thứ nhất trong VS Code tại root và chạy:

```powershell
.\scripts\run-server.ps1 -DatabasePath 'data/demo.db'
```

**Kết quả mong đợi:** Dòng `Auction server listening on 127.0.0.1:5555` xuất hiện và terminal tiếp tục chạy. Server chỉ nhận kết nối từ cùng máy theo cấu hình hiện tại. `run-server.ps1` tự chạy Maven `install -DskipTests` trước khi mở server; thời gian khởi động phụ thuộc lần build. **Nếu lỗi:** “Address already in use” nghĩa là cổng 5555 đang được dùng; dừng server cũ bằng `Ctrl+C` hoặc dùng `-Port 5556` rồi cấu hình client cho cùng cổng. Kiểm tra path DB. Có thể chỉnh cửa sổ/gia hạn cuối phiên bằng `-AntiSnipeWindowSeconds` và `-AntiSnipeExtensionSeconds`.

## 5. Mở nhiều client và thao tác

**Mục tiêu:** Thấy hai người đấu giá cùng một phiên.

**Thao tác:** Mở terminal PowerShell thứ hai và thứ ba tại root. Mỗi terminal chạy:

```powershell
.\scripts\run-client.ps1
```

**Kết quả mong đợi:** Mỗi lệnh mở một cửa sổ JavaFX ở màn đăng nhập. Đăng nhập `demo_seller` để xem/quản lý sản phẩm. Đăng nhập hai Bidder ở hai cửa sổ khác; mở cùng phiên RUNNING, đặt giá A rồi B cao hơn. Hai màn chi tiết nhận giá mới qua socket; lịch sử và biểu đồ cũng đổi. **Nếu lỗi:** Kiểm tra server vẫn chạy, cổng 5555 trống trước khi mở server, và Windows đang có desktop JavaFX. Client mặc định kết nối `127.0.0.1:5555`; script `run-client.ps1` hiện không có tham số host/port. `ClientMain` nhận `--host=... --port=...` khi dùng launcher tự cấu hình.

**Tạo phiên mới:** Trên màn Seller, chọn **Quản lý sản phẩm**, nhập tên, mô tả, giá khởi điểm và loại rồi nhấn **Thêm sản phẩm**. Chọn sản phẩm vừa tạo, nhấn **Giờ nhanh** để điền giờ phù hợp, rồi **Tạo phiên**. Đợi phiên thành RUNNING. Bidder mở **Xem chi tiết**, nhập giá lớn hơn giá đang hiển thị và nhấn **Đặt giá**. Khi hết giờ, server chuyển FINISHED và màn **Xem màn kết quả** hiện người thắng; nếu không có bid thì không có người thắng. Nếu bid bị từ chối, đọc thông báo, xem lại trạng thái phiên và nhập giá cao hơn.

## 6. Chạy demo và kiểm thử riêng

**Mục tiêu:** Lặp lại luồng kiểm chứng. **Thao tác:** `run-demo.ps1` gộp seed và chạy server trong terminal hiện tại; các client vẫn cần terminal riêng:

```powershell
.\scripts\run-demo.ps1
```

Để test tự động thường, chạy `./scripts/run-tests.ps1` khi không có Maven khác đang ghi cùng `target`. Test GUI cần server thật với DB thử và desktop: xem [GUI_GUIDE.md](GUI_GUIDE.md). Test mạng có `./scripts/test-network.ps1`. **Kết quả mong đợi:** `BUILD SUCCESS` hoặc báo cáo JUnit cụ thể. **Nếu lỗi:** Dừng server trước khi chạy test E2E riêng trên chính cổng 5555. Không dùng DB thật cho thử nghiệm xóa/sửa; xem [kịch bản demo](DEMO_SCENARIOS.md).

**Ghi nhớ:** Chạy server trước client; mỗi cửa sổ client là một kết nối riêng; giá chính thức do server và SQLite quyết định. Xem [luồng mã nguồn](CODE_WALKTHROUGH.md) để hiểu vì sao.
