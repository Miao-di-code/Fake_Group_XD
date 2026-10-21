# Bắt đầu học Online Auction System

**Mục đích:** Đây là mục lục cho toàn bộ tài liệu. Bạn có thể bắt đầu từ đây dù chưa từng học Java, Git, database hay Client–Server. Chỉ cần biết mở file Markdown trong VS Code hoặc bấm liên kết trên GitHub. Tài liệu giải thích đúng ứng dụng trong thư mục `D:\BTL_LTNC`.

## Dự án này là gì?

Online Auction System cho Seller đăng sản phẩm và mở phiên đấu giá, Bidder đặt giá, Admin xem/quản lý, rồi xác định người thắng khi hết giờ. Giao diện là ứng dụng **JavaFX desktop**; client nói chuyện với **server Java** qua **TCP Socket** bằng JSON; server lưu dữ liệu vào **SQLite**. Maven build ba module `common`, `server`, `client`. Khi một bid thành công, server gửi sự kiện cho các cửa sổ đang xem cùng phiên để giá đổi ngay.

Sau lộ trình này, bạn sẽ biết mở và chạy dự án, tìm file xử lý một thao tác, giải thích vì sao hai người đặt giá cùng lúc vẫn có kết quả nhất quán, đọc báo cáo test và phối hợp nhóm bằng Git/GitHub. Tài liệu [yêu cầu](REQUIREMENTS.md) và [nghiệm thu](FINAL_ACCEPTANCE_REPORT.md) phân biệt phần đã có, phần đã test và phần còn cần xác minh. Hiện server mặc định chỉ nhận kết nối `127.0.0.1`, nên đây chưa phải website công khai.

## Mục lục đầy đủ

| STT | File | Nội dung chính | Nên đọc khi nào |
| ---: | --- | --- | --- |
| 1 | [guide.md](guide.md) | Mục lục, thứ tự học và đường đọc theo bốn vai trò. | Mở đầu và khi cần tìm tài liệu. |
| 2 | [hdsd.md](hdsd.md) | Lệnh khởi động, cách cài bằng `environment.yml`, tài khoản mẫu và thao tác từng vai trò. | Khi chỉ muốn sử dụng ứng dụng. |
| 3 | [JAVA_BASICS.md](JAVA_BASICS.md) | Java, JDK/JVM, class, method, OOP, thread, exception, Maven bằng ví dụ trong dự án. | Trước khi mở mã Java lần đầu. |
| 4 | [REQUIREMENTS.md](REQUIREMENTS.md) | Mã yêu cầu B01–B15/O01–O04, hai PDF nguồn và các khác biệt giữa đề/hướng dẫn. | Khi muốn biết bài tập yêu cầu gì. |
| 5 | [ARCHITECTURE.md](ARCHITECTURE.md) | Sơ đồ hệ thống/lớp, ba module, MVC và Singleton/Factory/Observer. | Sau kiến thức Java cơ bản. |
| 6 | [GETTING_STARTED.md](GETTING_STARTED.md) | Từng bước cài, build, seed demo, chạy server và nhiều client trên Windows. | Khi chuẩn bị thực hành. |
| 7 | [GUI_GUIDE.md](GUI_GUIDE.md) | Các màn JavaFX, thao tác Seller/Bidder/Admin, test GUI và checklist bằng mắt. | Khi đã mở được giao diện. |
| 8 | [CODE_WALKTHROUGH.md](CODE_WALKTHROUGH.md) | Đi theo nút Đặt giá từ FXML đến service, SQLite và sự kiện quay lại màn hình. | Khi bắt đầu đọc source code. |
| 9 | [BACKEND_GUIDE.md](BACKEND_GUIDE.md) | API service, quyền, transaction, quy tắc bid, tạo Admin local. | Khi học code server. |
| 10 | [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) | SQLite, các bảng, khóa ngoại, migration và snapshot. | Khi học lưu trữ dữ liệu. |
| 11 | [API_PROTOCOL.md](API_PROTOCOL.md) | JSON từng dòng, request/response/event, các type và `SocketClient` cho JavaFX. | Khi học giao tiếp client/server. |
| 12 | [ADVANCED_FEATURES.md](ADVANCED_FEATURES.md) | Auto-bid, anti-sniping, biểu đồ và giới hạn đã ghi nhận. | Sau khi hiểu bid thường. |
| 13 | [TEST_CASES.md](TEST_CASES.md) | Các test Java, JUnit, integration test, cách đọc lỗi và coverage. | Trước khi sửa code hoặc chạy test. |
| 14 | [TEST_REPORT.md](TEST_REPORT.md) | Số liệu test lịch sử ở mốc trước nghiệm thu cuối. | Khi so sánh các mốc đo. |
| 15 | [FINAL_ACCEPTANCE_REPORT.md](FINAL_ACCEPTANCE_REPORT.md) | Đối chiếu yêu cầu → code → test, số liệu nghiệm thu và phần chưa xác minh. | Khi cần bằng chứng trạng thái dự án. |
| 16 | [PROJECT_STATE.md](PROJECT_STATE.md) | Bản tóm tắt trạng thái bàn giao, chức năng và giới hạn. | Khi cần xem nhanh hiện trạng. |
| 17 | [CONTRACTS.md](CONTRACTS.md) | Tên module/model/DTO/giao thức đã thống nhất, để kiểm tra khi sửa nhiều module. | Khi chuẩn bị đổi API hay schema. |
| 18 | [GIT_GITHUB_GUIDE.md](GIT_GITHUB_GUIDE.md) | Git/GitHub từ vựng và một vòng branch, commit, PR, review. | Trước khi làm việc nhóm. |
| 19 | [MODULE_OWNERSHIP.md](MODULE_OWNERSHIP.md) | Phân công bốn người và quy tắc review chéo. | Khi chia phần học hoặc việc nhóm. |
| 20 | [6_WEEK_PLAN.md](6_WEEK_PLAN.md) | Kế hoạch 6 tuần, file/test/phụ thuộc cho từng người; không phải lịch sử commit. | Khi xem tiến độ dự kiến. |
| 21 | [DEMO_SCENARIOS.md](DEMO_SCENARIOS.md) | Kịch bản demo 10–15 phút và bằng chứng E2E. | Trước buổi trình diễn. |
| 22 | [DEFENSE_QUESTIONS.md](DEFENSE_QUESTIONS.md) | Câu hỏi và đáp án ngắn về OOP, pattern, network, concurrency, CI. | Khi ôn bảo vệ. |

## Lộ trình học đề xuất

1. **Hiểu bài toán:** Đọc phần giới thiệu ở trên, rồi [REQUIREMENTS.md](REQUIREMENTS.md). Phân biệt Seller, Bidder, Admin và quy trình OPEN → RUNNING → FINISHED.
2. **Học đúng thuật ngữ cần dùng:** Đọc [JAVA_BASICS.md](JAVA_BASICS.md). Mở các file `Auction.java`, `User.java`, `ItemFactory.java` được chỉ dẫn.
3. **Nhìn cấu trúc:** Đọc [ARCHITECTURE.md](ARCHITECTURE.md) để biết `common`, `server`, `client`, FXML và database ở đâu.
4. **Hiểu đường đi dữ liệu:** Đọc [API_PROTOCOL.md](API_PROTOCOL.md) và [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md). Tự trả lời request, response và event khác nhau thế nào.
5. **Chạy thật:** Làm từng bước trong [GETTING_STARTED.md](GETTING_STARTED.md), rồi xem [GUI_GUIDE.md](GUI_GUIDE.md). Mở một server và ít nhất hai client.
6. **Đọc chức năng từ code:** Lần theo [CODE_WALKTHROUGH.md](CODE_WALKTHROUGH.md), rồi [BACKEND_GUIDE.md](BACKEND_GUIDE.md) và [ADVANCED_FEATURES.md](ADVANCED_FEATURES.md).
7. **Kiểm thử:** Đọc [TEST_CASES.md](TEST_CASES.md), chạy test và so với [FINAL_ACCEPTANCE_REPORT.md](FINAL_ACCEPTANCE_REPORT.md). Báo cáo chứa số lịch sử; terminal trên máy bạn là kết quả mới.
8. **Làm việc nhóm:** Đọc [GIT_GITHUB_GUIDE.md](GIT_GITHUB_GUIDE.md), [MODULE_OWNERSHIP.md](MODULE_OWNERSHIP.md) và [6_WEEK_PLAN.md](6_WEEK_PLAN.md). Khi thay đổi giao thức, tra [CONTRACTS.md](CONTRACTS.md).
9. **Chuẩn bị báo cáo:** Dùng [DEMO_SCENARIOS.md](DEMO_SCENARIOS.md), [DEFENSE_QUESTIONS.md](DEFENSE_QUESTIONS.md) và [PROJECT_STATE.md](PROJECT_STATE.md) để trình bày điều đã kiểm chứng và giới hạn.

## Lộ trình theo vai trò bốn người

Mỗi người đọc các bước 1–5 ở trên trước. Sau đó tập trung phần mình phụ trách, nhưng vẫn phải hiểu toàn bộ đường đi GUI → TCP → service → SQLite → event để giải thích dự án.

| Người | Đọc sâu trước | Mở code trước |
| --- | --- | --- |
| 1 – Auction Core | [CODE_WALKTHROUGH.md](CODE_WALKTHROUGH.md), [BACKEND_GUIDE.md](BACKEND_GUIDE.md), [ADVANCED_FEATURES.md](ADVANCED_FEATURES.md), [TEST_CASES.md](TEST_CASES.md) | `common/.../model/Auction.java`, `BidTransaction.java`; `server/.../service/AuctionService.java`, `AuctionManager.java`, `AutoBidCalculator.java`, `AntiSnipingPolicy.java` |
| 2 – JavaFX GUI | [GUI_GUIDE.md](GUI_GUIDE.md), [API_PROTOCOL.md](API_PROTOCOL.md), [CODE_WALKTHROUGH.md](CODE_WALKTHROUGH.md) | `client/src/main/resources/view/detail.fxml`; `client/.../controller/DetailController.java`, `SellerController.java`; `client/.../view/AppController.java` |
| 3 – Networking | [API_PROTOCOL.md](API_PROTOCOL.md), [ARCHITECTURE.md](ARCHITECTURE.md), [TEST_CASES.md](TEST_CASES.md) | `common/.../protocol/RequestMessage.java`; `server/.../network/SocketServer.java`, `RequestDispatcher.java`, `AuctionEventBroadcaster.java`; `client/.../network/SocketClient.java` |
| 4 – User/Product/Database/CI | [BACKEND_GUIDE.md](BACKEND_GUIDE.md), [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md), [GIT_GITHUB_GUIDE.md](GIT_GITHUB_GUIDE.md), [TEST_CASES.md](TEST_CASES.md) | `server/.../service/AuthService.java`, `ProductService.java`; `server/.../dao/UserDao.java`, `ItemDao.java`; `server/src/main/resources/db/schema.sql`; `.github/workflows/ci.yml` |

Trong bảng, `...` là phần thư mục `src/main/java/com/btl/auction/<module>`; dùng tên file để tìm trong VS Code. Nếu chỉ muốn dùng ứng dụng, vào [hdsd.md](hdsd.md). Nếu muốn tự giải thích code, làm xong các bước 1–7 rồi thử trả lời [DEFENSE_QUESTIONS.md](DEFENSE_QUESTIONS.md).
