# Hướng Dẫn Khởi Chạy Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)

Tài liệu này tóm tắt toàn bộ các bước để mở Server, khởi chạy nhiều Client đấu giá song song, danh sách tài khoản thử nghiệm và cách reset dữ liệu khi phiên đấu giá kết thúc.

---

## ⚠️ Nguyên Tắc Vàng Khi Mở Cửa Sổ PowerShell Mới

Dự án sử dụng bộ công cụ **JDK 17** và **Maven** được đóng gói riêng trong thư mục `_env`. Do đó, **bất cứ khi nào mở một cửa sổ PowerShell mới**, bạn hãy chạy ngay 2 lệnh tiền đề này trước:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\scripts\Use-LocalConda.ps1
```

*(Lệnh này cấp quyền chạy script và tự động nạp `java`, `mvn` của dự án vào cửa sổ hiện tại).*

---

## 1. Quy Trình Khởi Động Hàng Ngày (Server trước - Client sau)

### Bước 1: Bật Server (Cửa sổ PowerShell 1)
1. Mở PowerShell tại thư mục dự án `D:\Fake_Group_XD`.
2. Chạy lệnh nạp môi trường:
   ```powershell
   Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
   .\scripts\Use-LocalConda.ps1
   ```
3. Khởi động Server:
   ```powershell
   .\scripts\run-server.ps1 -DatabasePath 'data/demo.db'
   ```
   *Hoặc dùng lệnh Maven tương đương:*
   ```powershell
   mvn -pl server exec:java -Dexec.args="5555 data/demo.db"
   ```
4. **Trạng thái thành công:** Màn hình in ra dòng `Auction server listening on 127.0.0.1:5555`.
5. 🛑 **Lưu ý:** Giữ nguyên cửa sổ này để Server duy trì hoạt động, không tắt và không bấm `Ctrl + C`.

---

### Bước 2: Bật Client 1 (Cửa sổ PowerShell 2)
1. Mở một cửa sổ PowerShell thứ hai tại `D:\Fake_Group_XD`.
2. Chạy lệnh:
   ```powershell
   Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
   .\scripts\Use-LocalConda.ps1
   mvn -pl client javafx:run
   ```
3. Cửa sổ giao diện đăng nhập đồ họa JavaFX sẽ hiện lên.

---

### Bước 3: Bật Client 2 để test Real-time (Cửa sổ PowerShell 3)
1. Mở thêm một cửa sổ PowerShell thứ ba tại `D:\Fake_Group_XD`.
2. Chạy lệnh tương tự:
   ```powershell
   Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
   .\scripts\Use-LocalConda.ps1
   mvn -pl client javafx:run
   ```
3. Cửa sổ ứng dụng thứ hai sẽ bật lên độc lập.

---

## 2. Danh Sách Tài Khoản Thử Nghiệm

Tất cả tài khoản mẫu đều dùng chung mật khẩu mặc định: **`DemoPass123!`**

| Tài khoản | Quyền hạn | Mục đích sử dụng |
| :--- | :--- | :--- |
| **`demo_seller`** | Người bán (Seller) | Thêm/sửa sản phẩm, tạo và mở phiên đấu giá mới |
| **`demo_bidder_a`** | Người mua (Bidder 1) | Tham gia đặt giá thủ công hoặc cài đặt Auto-bid |
| **`demo_bidder_b`** | Người mua (Bidder 2) | Dùng để đấu giá cạnh tranh trực tiếp với Bidder A |
| **`demo_admin`** | Quản trị viên (Admin) | Quản lý người dùng, xem toàn bộ hệ thống |

---

## 3. Cách Test Đấu Giá Thời Gian Thực (Real-time Socket)

1. Đặt 2 cửa sổ Client cạnh nhau trên màn hình desktop:
   * **Cửa sổ trái:** Đăng nhập `demo_bidder_a`.
   * **Cửa sổ phải:** Đăng nhập `demo_bidder_b`.
2. Cả hai tài khoản cùng chọn một phiên đang ở trạng thái **`RUNNING`** (ví dụ phiên *Demo Laptop*) $\rightarrow$ bấm **Xem chi tiết**.
3. **Thao tác kiểm tra:**
   * **Bidder A** nhập một mức giá cao hơn giá hiện tại và bấm **Đặt giá (Bid)**.
   * **Quan sát cửa sổ của Bidder B:** Màn hình của Bidder B tự động nhảy số tiền mới nhất, tên người dẫn đầu chuyển thành `demo_bidder_a`, và biểu đồ lịch sử giá vẽ thêm điểm mới ngay lập tức mà không cần bấm tải lại.
   * **Bidder B** đặt giá cao hơn tiếp tục để thấy cửa sổ của **Bidder A** nhảy số.
   * Nhập thử giá thấp hơn giá hiện tại để kiểm tra cơ chế kiểm soát ngoại lệ (`InvalidBidException`).

---

## 4. Cách Tạo Lại Dữ Liệu Mẫu (Khi Phiên Đấu Giá Hết Giờ)

Phiên đấu giá mẫu được thiết lập có thời hạn khoảng 30 phút. Nếu sau này mở lên thấy các phiên đều đã ở trạng thái `FINISHED` (kết thúc), bạn có thể nạp lại dữ liệu ban đầu:

1. Tại cửa sổ **Server**, nhấn tổ hợp phím **`Ctrl + C`** để dừng Server tạm thời.
2. Chạy lệnh nạp lại cơ sở dữ liệu:
   ```powershell
   mvn -pl server exec:java '-Dexec.mainClass=com.btl.auction.server.service.DemoSeeder' '-Dexec.args=data/demo.db'
   ```
3. Khởi động lại Server:
   ```powershell
   .\scripts\run-server.ps1 -DatabasePath 'data/demo.db'
   ```

---

## 5. Bảng Tra Cứu Lỗi Nhanh

| Hiện tượng | Nguyên nhân | Cách khắc phục |
| :--- | :--- | :--- |
| `'mvn' is not recognized...` | Chưa nạp môi trường cục bộ | Chạy `.\scripts\Use-LocalConda.ps1` |
| `File ... cannot be loaded because running scripts is disabled` | PowerShell chặn script do bảo mật | Chạy `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass` |
| `Unrecognized option: Hieu/...` | Đường dẫn thư mục có dấu cách | Chạy thư mục từ ổ `D:\Fake_Group_XD` (không để trong thư mục người dùng có khoảng trắng) |
| Client không kết nối được | Server chưa được khởi động trước | Mở Server ở Cửa sổ 1 trước, đảm bảo hiện `Auction server listening...` rồi mới bật Client |