# Đối chiếu yêu cầu

**Mục đích:** Giữ mã yêu cầu B01–B15/O01–O04 và vị trí trong hai PDF gốc. Người mới nên đọc [guide.md](guide.md) trước. Bảng này dùng để hiểu tiêu chí, không thay cho bằng chứng đã chạy ở [FINAL_ACCEPTANCE_REPORT.md](FINAL_ACCEPTANCE_REPORT.md).

Nguồn: [Đề bài](../requirements/2026-Bài%20tập%20lớn.pdf) (10 trang) và [Hướng dẫn](../requirements/Hướng%20dẫn%20BTL%20LTNC-2026%20%281%29.pdf) (7 trang). Số trang dưới đây là số trang PDF, không phải số in ở chân trang. Cột triển khai ghi thành phần được nhắm đến trong kế hoạch; trạng thái thực tế cần đối chiếu với báo cáo nghiệm thu và source.

## Bắt buộc

| ID | Yêu cầu gốc | Nguồn | Thành phần dự kiến | Kiểm chứng |
| --- | --- | --- | --- | --- |
| B01 | Đăng ký, đăng nhập; Bidder, Seller, Admin có quyền riêng | Đề tr. 4; HD tr. 1 | User và lớp con; AuthService, UserDao; màn Login/Register | Đăng ký, đăng nhập, phân quyền, từ chối sai vai trò |
| B02 | Seller thêm, sửa, xóa sản phẩm; tên, mô tả, giá khởi điểm | Đề tr. 4; HD tr. 1 | Item và lớp con; ProductService, ItemDao; màn Seller | CRUD và kiểm tra quyền sở hữu |
| B03 | Hiển thị giá cao nhất và thời gian bắt đầu/kết thúc | Đề tr. 4–5 | Auction, AuctionDao, AuctionDto, màn chi tiết | Thông tin khớp dữ liệu server |
| B04 | Bid phải cao hơn giá hiện tại, kiểm tra hợp lệ, cập nhật người dẫn đầu | Đề tr. 5; HD tr. 1 | AuctionService, BidDao, BidTransaction | Bid hợp lệ; giá bằng/thấp hơn bị từ chối |
| B05 | Tự đóng khi hết giờ, xác định người thắng; OPEN → RUNNING → FINISHED → PAID/CANCELED | Đề tr. 5; HD tr. 1, 3 | AuctionService, scheduler, AuctionStatus | Test ranh giới thời gian và người thắng |
| B06 | Xử lý bid thấp, phiên đã đóng, dữ liệu lỗi, mất kết nối | Đề tr. 5; HD tr. 1, 3 | Domain exception, mã lỗi protocol, client thông báo | Test lỗi nghiệp vụ và ngắt socket |
| B07 | GUI danh sách, chi tiết, đấu giá realtime, quản lý Seller; đăng nhập/đăng ký theo HD | Đề tr. 6; HD tr. 4 | JavaFX/FXML controllers và view | Luồng thao tác GUI với server |
| B08 | OOP: đóng gói, kế thừa, đa hình, trừu tượng; User/Item/Auction/Bid | Đề tr. 8; HD tr. 1–2 | common.model | Test hành vi lớp con, review sơ đồ lớp |
| B09 | Singleton, Factory Method, Observer phù hợp | Đề tr. 9; HD tr. 1–3 | AuctionManager, ItemFactory, AuctionListener | Test một manager, loại item, sự kiện |
| B10 | Client–Server, JSON qua Socket hoặc REST; client MVC/FXML, server Controller → Model → DAO; chỉ server chạm DB | Đề tr. 8–9; HD tr. 1, 4–5 | common.protocol, server.network, server.service/dao, client | Chạy nhiều client; kiểm tra phụ thuộc module |
| B11 | Bid đồng thời an toàn, tránh lost update/race condition/rollback/hai người thắng | Đề tr. 7, 10; HD tr. 1, 3, 6 | khóa theo auction + transaction SQLite | Stress test đồng thời, một giá/người thắng |
| B12 | Observer/Socket đẩy bid mới đến mọi client đang xem, không polling liên tục | Đề tr. 7, 10; HD tr. 1, 4, 6 | server.event và broadcast socket | Hai client cùng xem nhận event |
| B13 | Maven/Gradle, mã sạch, coding convention, JUnit logic quan trọng, CI GitHub Actions | Đề tr. 9–10; HD tr. 1, 4, 6 | pom, Checkstyle, JaCoCo, tests, workflow | mvn clean verify và CI xanh |
| B14 | Lưu/tải dữ liệu bằng Serialization/File I/O theo hướng dẫn tự học | HD tr. 2, 4–5, 7 | SQLite/JDBC chính; Gson export/import snapshot phụ trợ | Export, import và đối chiếu dữ liệu |
| B15 | Mỗi thành viên hiểu toàn bộ mã, đóng góp minh bạch, commit thường xuyên | Đề tr. 3; HD tr. 1, 7 | MODULE_OWNERSHIP, kế hoạch, review chéo | Lịch sử commit thật và demo từng thành viên |

## Nâng cao tùy chọn

| ID | Yêu cầu | Nguồn | Thành phần dự kiến | Kiểm chứng |
| --- | --- | --- | --- | --- |
| O01 | Auto-bidding: maxBid, increment, ưu tiên đăng ký sớm, xung đột đồng thời | Đề tr. 6, 10; HD tr. 1, 5–6 | `AuctionService`, `AutoBidCalculator`, `AutoBidDao`, bảng `auto_bids` | Test hai auto-bid, giá trần, hòa ưu tiên |
| O02 | Anti-sniping: bid trong X giây cuối gia hạn Y giây | Đề tr. 7, 10; HD tr. 1, 5–6 | AuctionService cấu hình X/Y | Test mốc thời gian sát hạn |
| O03 | Biểu đồ giá theo timestamp cập nhật ngay khi bid hợp lệ | Đề tr. 7, 10; HD tr. 1, 5–6 | JavaFX LineChart + bid event | Biểu đồ thay đổi không refresh |
| O04 | Strategy/Command cho kiểu bid khác nhau | Đề tr. 9 | Chỉ thêm nếu logic thực sự cần | Review không tạo abstraction thừa |

## Kỹ thuật, kiểm thử và đánh giá

- Java 17, Maven multi-module common/server/client, JavaFX/FXML, TCP UTF-8 JSON một object mỗi dòng, Gson, SQLite/JDBC, JUnit 5, GitHub Actions, Checkstyle, JaCoCo là lựa chọn cố định của dự án.
- Chỉ server có JDBC/SQLite. Tiền là long VND. Mốc lưu trữ dùng epoch milliseconds UTC; giao diện đổi sang múi giờ máy.
- Đề tr. 10 và HD tr. 6 cùng cho điểm: lớp/kế thừa 0,5; OOP 1; pattern 1; user/product 1; đấu giá 1; lỗi 1; concurrency 1; realtime 0,5; client–server 0,5; MVC 0,5; build/code style 0,5; JUnit 0,5; CI 0,5. Nâng cao: auto-bid 0,5; anti-sniping 0,5; biểu đồ 0,5; tối đa 1,5 điểm cộng. Bảng nguồn ghi tổng 10+1 dù ba mục cộng 0,5; giữ nguyên cả hai thông tin, hỏi giảng viên khi cần chốt cách tính.
- HD tr. 4 yêu cầu mục tiêu coverage ≥ 60% vào tuần 10. Đo bằng JaCoCo, tập trung logic quan trọng; không coi coverage thay thế test nghiệp vụ.
- Kiểm thử cuối: unit test nghiệp vụ và protocol; integration test DAO/SQLite; nhiều socket client đồng thời; GUI end-to-end; CI chạy từ root; demo từng thành viên.

## Khác biệt và quyết định

| Khác biệt | Nội dung hai nguồn | Quyết định |
| --- | --- | --- |
| Thời lượng | Prompt giao 6 tuần; HD tr. 1 nói còn 9 tuần, lộ trình tuần 6–14 và trình bày tuần 15 | Lập kế hoạch nội bộ 6 tuần trong 6_WEEK_PLAN.md; vẫn giữ mốc trình bày và học phần theo HD nếu lịch lớp áp dụng |
| GUI | Đề tr. 6 cho JavaFX hoặc Swing; HD tr. 1 yêu cầu JavaFX | Chọn JavaFX + FXML |
| Giao tiếp | Đề tr. 8 cho REST hoặc Socket; HD tr. 4–5 hướng Socket | Chọn Java Socket TCP + JSON |
| Build | Đề tr. 9 cho Maven hoặc Gradle; HD tr. 4 ghi Maven | Chọn Maven |
| CI | Đề tr. 9 mô tả “khuyến khích”; bảng điểm tr. 10 và HD tr. 1, 6 coi là bắt buộc | Triển khai GitHub Actions |
| Serialization | Đề không chỉ định kho lưu; HD tr. 7 coi lưu trữ/Serialization là yêu cầu | SQLite là nguồn chính; bổ sung export/import JSON bằng Gson/File I/O |
| Bid đồng thời, realtime | Đề đặt trong mục 3.2 “nâng cao” nhưng bảng điểm tr. 10 và HD tr. 1, 6 ghi bắt buộc | Xử lý concurrency và realtime như yêu cầu bắt buộc |

Tiến độ thực tế của từng mục được ghi trong [PROJECT_STATE.md](PROJECT_STATE.md). **Ghi nhớ:** B15 (lịch sử đóng góp thật) chưa xác minh; O04 Strategy/Command chưa triển khai vì code hiện tại không cần thêm lớp trừu tượng. Xem [guide.md](guide.md) để tiếp tục học theo thứ tự.
