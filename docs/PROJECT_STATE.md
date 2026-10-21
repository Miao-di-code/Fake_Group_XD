# Trạng thái bàn giao sau Prompt 6/6

Cập nhật 19/10/2026 (Asia/Saigon), root `D:\BTL_LTNC`. Dự án Java 17/Maven ba module `common`, `server`, `client` chạy trên Windows: JavaFX/FXML → TCP JSON → service/DAO → SQLite, realtime qua Observer/socket. Chi tiết đối chiếu từng yêu cầu PDF ở [FINAL_ACCEPTANCE_REPORT.md](FINAL_ACCEPTANCE_REPORT.md).

## Bằng chứng cuối

- `mvn -B clean verify` **BUILD SUCCESS** lúc 18:47:53 +07; JUnit **42/42 PASS** (common 8, server 30, client 4), 0 failure/error/skip; Checkstyle 0 violation.
- JaCoCo line coverage: common **159/229 = 69,4%**, server **936/1076 = 87,0%**, client **118/734 = 16,1%**. Gate server ≥60% đạt.
- E2E TCP thật chạy mặc định và riêng `127.0.0.1:5555`: Seller + hai Bidder, bid đồng thời, broadcast, FINISHED, restart Server, SQLite vẫn giữ item/giá/winner/history.
- GUI `GuiFlowIT` chạy riêng **1/1 PASS** (51,40 giây) với ba cửa sổ JavaFX/server thật: auth, Seller tạo item/auction, realtime hai chiều, auto-bid qua form, LineChart cập nhật, kết thúc và winner. `GuiAdminIT` **1/1 PASS** (4,98 giây): sai mật khẩu nhận `AUTH_FAILED`, Admin xem 3 item/3 auction, logout. `run-client.ps1` mở được cửa sổ login thật. `run-demo.ps1` seed và mở server; PING thành công.
- `DemoSeeder` tạo bốn tài khoản/ba sản phẩm/ba phiên, chạy lại không trùng; DB thử do quá trình nghiệm thu tạo đã dọn. Không có `.git` tại workspace, không commit/push.

## Chức năng và giới hạn

Chức năng bắt buộc về OOP/pattern, auth/role, CRUD, auction lifecycle, transaction/concurrency, realtime, TCP/JSON, GUI MVC, lỗi, snapshot, Maven/JUnit/Checkstyle đã có và được test theo mức ghi trong báo cáo. Auto-bid, anti-sniping, biểu đồ lịch sử đã triển khai và kiểm thử service/socket; auto-bid/chart cũng có GUI E2E. CI YAML đã cấu hình nhưng **chưa có kết quả GitHub Actions thực tế**. Lịch sử commit và đóng góp thật của bốn sinh viên không thể xác minh vì workspace không có `.git`.

**Manual GUI verification required:** bố cục trực quan, thao tác Admin đổi trạng thái và Seller sửa/xóa, countdown sau anti-sniping trên màn hình. Coverage client còn thấp (16,1%). Server mặc định localhost, không TLS; token trong bộ nhớ mất khi restart; PAID là dấu trạng thái, chưa tích hợp thanh toán thật. Không tuyên bố hoàn thành 100% tiêu chí lớp học khi các mục này chưa được kiểm chứng/hoàn tất.

## Ghi chú đối chiếu tài liệu ngày 21/10/2026

- Tài liệu cũ nêu `AutoBidService` như thành phần dự kiến; code thực tế dùng `AuctionService`, `AutoBidCalculator` và `AutoBidDao`. [REQUIREMENTS.md](REQUIREMENTS.md) đã ghi đúng tên.
- Tài liệu backend cũ nói snapshot có bốn bảng; `SnapshotService` hiện dùng năm bảng ở v2, gồm `auto_bids`, và vẫn đọc v1. [BACKEND_GUIDE.md](BACKEND_GUIDE.md) đã làm rõ.
- `ItemFactory.create` trong code là factory tĩnh. [ARCHITECTURE.md](ARCHITECTURE.md) giải thích cách dùng và phân biệt với Factory Method theo nghĩa mẫu thiết kế đầy đủ.
- Các mục trên là sửa mô tả cho khớp code, không thay đổi kết quả nghiệm thu lịch sử ở trên. Tài liệu bắt đầu ở [guide.md](guide.md).

## Chạy và tài liệu

Xem [guide.md](guide.md) để bắt đầu đọc, [GETTING_STARTED.md](GETTING_STARTED.md) cho các bước cài/chạy/test, và [README.md](../README.md) để tra nhanh lệnh. `scripts/run-tests.ps1`, `seed-demo.ps1`, `run-demo.ps1`, `run-server.ps1`, `run-client.ps1` chạy từ root. Tài liệu cho bốn người: [ARCHITECTURE.md](ARCHITECTURE.md), [CODE_WALKTHROUGH.md](CODE_WALKTHROUGH.md), [MODULE_OWNERSHIP.md](MODULE_OWNERSHIP.md), [DEFENSE_QUESTIONS.md](DEFENSE_QUESTIONS.md), [DEMO_SCENARIOS.md](DEMO_SCENARIOS.md), [6_WEEK_PLAN.md](6_WEEK_PLAN.md). Kế hoạch commit là đề xuất, không phải lịch sử Git.
