# Báo cáo kiểm thử lịch sử: mốc trước nghiệm thu cuối

**Mục đích:** Giữ số liệu đã đo ở mốc trước, để so với [nghiệm thu cuối](FINAL_ACCEPTANCE_REPORT.md). Người mới nên đọc [TEST_CASES.md](TEST_CASES.md) để hiểu JUnit/coverage trước. Các số dưới đây là kết quả ngày ghi nhận, không phải kết quả tự động của máy hiện tại.

Đây là số liệu lịch sử sau Prompt 5; số liệu nghiệm thu mới nhất sau Prompt 6 ở [FINAL_ACCEPTANCE_REPORT.md](FINAL_ACCEPTANCE_REPORT.md).

Ngày 19/10/2026, môi trường Windows PowerShell, JDK 17/Maven và Maven repository trong `_env`. Lệnh đã chạy tại root: `scripts/Use-LocalConda.ps1`, sau đó `mvn -o -B clean verify` (offline vì môi trường dùng repository cục bộ). Workflow GitHub dùng `mvn -B clean verify` khi có mạng.

| Module | JUnit đạt | Failure/Error/Skip | JaCoCo line coverage |
| --- | ---: | ---: | ---: |
| common | 8 | 0/0/0 | 159/229 = 69,4% |
| server | 28 | 0/0/0 | 902/1036 = 87,1% |
| client | 4 | 0/0/0 | 118/734 = 16,1% |
| Tổng | **40** | **0/0/0** | — |

Kết quả cuối: **BUILD SUCCESS** lúc 18:18:47 +07; Checkstyle 0 violation; ngưỡng JaCoCo server ≥60% đạt. Báo cáo HTML/XML ở `*/target/site/jacoco`, kết quả JUnit XML ở `*/target/surefire-reports`.

Các đường kiểm tra chính: auth và role, product CRUD/ItemFactory, auction lifecycle/winner/auto close, transaction rollback, bid đồng thời 24 tác vụ, nhiều socket và broadcast, request lỗi/mất kết nối, DB save/restart và snapshot v1-v2. Prompt 5 thêm test auto-bid giới hạn giá và hòa ưu tiên, bid đồng thời khi auto-bid hoạt động, anti-sniping tại ranh giới thời gian và scheduler, network nhận `BID_UPDATED`/`AUCTION_EXTENDED`, client không ghi đè end time mới bằng event cũ, migration database v1.

Giới hạn: coverage client thấp vì controller JavaFX chưa có test thao tác UI đầy đủ; `GuiFlowIT` desktop của Prompt 4 chưa được chạy lại sau Prompt 5. CI YAML mới chỉ được xác nhận qua build Maven cục bộ; chưa có lần chạy GitHub Actions. Không có test giả thay thế business service bằng dữ liệu cứng trong integration test.
