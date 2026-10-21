# Nghiệm thu cuối dự án (Prompt 6/6)

**Ngày:** 19/10/2026 (Asia/Saigon). **Root:** `D:\BTL_LTNC`. **Môi trường:** Windows PowerShell; Azul OpenJDK 17.0.18, Maven 3.9.16 trong `_env`. Hai PDF nguồn nằm trong `requirements/`; mã B01–B15/O01–O03 và trang nguồn ở [REQUIREMENTS.md](REQUIREMENTS.md). Bảng dưới đối chiếu từng nhóm yêu cầu đó với code/test thật.

## Requirement → class/file → test → kết quả

| Yêu cầu PDF | Class/file chính | Test/bằng chứng | Kết quả |
| --- | --- | --- | --- |
| B01 User, Register/Login, vai trò/quyền | `AuthService`, `UserDao`, `RequestDispatcher`, Login/Register FXML | `BackendIntegrationTest.registrationLoginSessionAndAdminRules`, `SocketServerIntegrationTest` | PASS tự động |
| B02 CRUD sản phẩm | `ProductService`, `ItemDao`, `SellerController` | `productOwnershipAndAuctionProtection`, GUI flow tạo item | PASS tự động; thao tác sửa/xóa GUI cần kiểm tra tay |
| B03 Giá/giờ phiên | `AuctionService`, `AuctionDto`, `DetailController` | backend/network integration, GUI flow | PASS tự động |
| B04 Đặt giá, validation, leader | `AuctionService.placeBid`, `BidDao` | `biddingTransitionsWinnerAndFinalization`, final E2E, giá âm/bằng | PASS tự động |
| B05 Auto close, winner, trạng thái | `AuctionScheduler`, `AuctionService.closeExpired/finalizeAuction` | `biddingTransitionsWinnerAndFinalization`, `noBidMeansNoWinnerAndCancellation`, final E2E | PASS tự động |
| B06 Xử lý lỗi | `ServiceException`, `RequestDispatcher`, `ClientHandler`, `SocketClient` | auth/bid/rollback tests, malformed JSON, disconnect tests | PASS tự động |
| B07 GUI JavaFX/FXML/MVC | `ClientMain`, 7 FXML, controllers, `AppController` | `FxmlContractTest`, `GuiFlowIT` ba Stage, `GuiAdminIT` qua server thật | PASS các luồng đã test; manual visual/Admin đổi trạng thái cần thêm |
| B08 OOP: đóng gói/kế thừa/đa hình/trừu tượng | `Entity`, `User`/lớp con, `Item`/lớp con, `Auction`, `BidTransaction` | `ModelTest`, [ARCHITECTURE.md](ARCHITECTURE.md) | PASS code/test/sơ đồ |
| B09 Singleton/Factory/Observer | `AuctionManager`, `ItemFactory`, `AuctionEventPublisher` | `AuctionManagerTest`, `ModelTest`, `AuctionEventPublisherTest` | PASS tự động |
| B10 Client–Server TCP JSON, chỉ server dùng DB | `SocketServer`, `SocketClient`, `JsonCodec`, `server.dao` | `SocketServerIntegrationTest`, `SocketClientTest`, module POM | PASS tự động |
| B11 Concurrency/rollback | `AuctionManager`, `Transactions`, `AuctionService`, `AuctionDao` | `manyThreadsDoNotLoseAcceptedBids`, `databaseFailureRollsBackBidAndDoesNotPublish`, final E2E | PASS tự động |
| B12 Realtime Observer/broadcast | `AuctionEventPublisher`, `AuctionEventBroadcaster`, `DetailController` | hai Bidder trong socket/GUI E2E, BID_UPDATED/FINISHED | PASS tự động |
| B13 Maven/JUnit/Checkstyle/CI | root POM, `config/checkstyle.xml`, `.github/workflows/ci.yml` | clean verify cục bộ, 0 Checkstyle violation | PASS cục bộ; GitHub Actions chưa chạy |
| B14 Serialization/File I/O | `SnapshotService` | `snapshotRoundTripKeepsRelatedRowsAndRejectsOverwrite`, snapshot v1/v2 test | PASS tự động |
| B15 Phân công/lịch sử đóng góp thực tế | `MODULE_OWNERSHIP.md`, `6_WEEK_PLAN.md` | kế hoạch 4 người/6 tuần có file/test/phụ thuộc | Chưa xác minh lịch sử Git/công sức từng người; workspace không có `.git` |
| O01 Auto-bidding | `AutoBidCalculator`, `AutoBidDao`, `AuctionService`, `DetailController` | calculator/integration/concurrency/network/GUI flow | PASS các nhánh đã test; proxy một bước theo quy tắc tài liệu |
| O02 Anti-sniping | `AntiSnipingPolicy`, `AuctionService`, `AuctionDao` | boundary + scheduler + socket event tests | PASS tự động; thao tác nhìn countdown gia hạn cần kiểm tra tay |
| O03 Bid history visualization | `DetailController`, `detail.fxml` LineChart | GUI flow xác nhận số điểm chart tăng sau BID_UPDATED | PASS tự động, chưa đánh giá thẩm mỹ trực quan |

## Build, test và coverage

Lệnh đã chạy tại root sau `scripts/Use-LocalConda.ps1`: `scripts/run-tests.ps1` (`mvn -o -B clean verify`) và riêng **`mvn -B clean verify` đúng theo yêu cầu**. Lần chạy cuối **BUILD SUCCESS lúc 18:47:53 +07**. JUnit mặc định **42/42 PASS**: common 8, server 30, client 4; 0 failure, 0 error, 0 skipped. Checkstyle **0 violation**. JaCoCo line coverage thực đo: common **159/229 = 69,4%**, server **936/1076 = 87,0%**, client **118/734 = 16,1%**. Ngưỡng server ≥60% trong `server/pom.xml` đạt. `GuiFlowIT` và `GuiAdminIT` mỗi bài 1/1 PASS khi chạy riêng vì cần desktop/server thật, không cộng vào 42 test mặc định. Surefire XML và JaCoCo XML/HTML ở `*/target/surefire-reports` và `*/target/site/jacoco`.

Lệnh E2E cổng 5555 đã chạy: `mvn -o -B -pl server -am test '-Dtest=SocketServerIntegrationTest#finalEndToEndFlowSurvivesServerRestartAndKeepsWinner' '-Dsurefire.failIfNoSpecifiedTests=false' '-Dauction.e2e.port=5555'`. Lệnh GUI đã chạy: `scripts/test-gui.ps1` khi `scripts/run-server.ps1 -DatabasePath data/gui-final-advanced.db -AntiSnipeWindowSeconds 0 -AntiSnipeExtensionSeconds 0` phục vụ DB thử; `scripts/test-gui.ps1 -Admin` khi `scripts/run-demo.ps1 -DatabasePath data/gui-admin-check.db` phục vụ DB seed. `scripts/run-client.ps1` cũng đã chạy; các file DB thử được xóa sau khi dừng tiến trình.

## E2E và vận hành đã chạy

- Final E2E qua TCP/JSON thật: Seller đăng nhập/tạo item/auction; hai Bidder lấy chi tiết, subscribe, bid hai chiều; giá bằng bị từ chối; hai bid đồng thời không mất history; FINISHED broadcast; bid sau hạn bị từ chối; đóng và khởi động lại `SocketServer` trên cùng SQLite; item, giá, winner, status, history khớp. Chạy mặc định cổng tạm trong verify và chạy riêng trên **localhost:5555**, đều PASS. Clock trong test điều khiển thời gian để không chờ thật; scheduler vẫn phát sự kiện tự động.
- `GuiFlowIT` chạy trên Windows desktop với server thật/DB tạm: ba cửa sổ JavaFX, register/login, Seller tạo item/auction, hai Bidder đặt giá và nhận realtime, auto-bid từ form, biểu đồ tăng điểm theo lịch sử server, tắt auto-bid, FINISHED và winner. **1/1 test PASS, 51,40 giây**. FXML load test cũng PASS.
- `GuiAdminIT` chạy với server/demo DB thật: sai mật khẩu hiển thị `AUTH_FAILED`, Admin login, thấy 3 item/3 auction, quay lại danh sách và logout về màn login. **1/1 PASS, 4,98 giây** ở lần chạy cuối. Thao tác Admin đổi trạng thái và Seller sửa/xóa trên GUI chưa có click test; **Manual GUI verification required** cho các nhánh đó, bố cục trực quan và countdown anti-sniping.
- `scripts/seed-demo.ps1` đã chạy với DB tạm: `Demo ready: 4 users, 3 items, 3 auctions`; `DemoSeederTest` xác nhận chạy lặp không trùng, OPEN/RUNNING và mật khẩu hash. `run-demo.ps1` đã seed rồi khởi động ServerMain trên port 5555; TCP PING trả `pong=true`. `run-client.ps1` đã mở cửa sổ `Online Auction System - login` qua JavaFX plugin. DB thử đã xóa sau khi dừng server/client, không đụng DB người dùng.

## Edge cases

JUnit đã kiểm tra username trùng, password sai, role sai, giá âm/bằng, phiên chưa bắt đầu/đã kết thúc, bid đồng thời, không có bid khi kết thúc, client ngắt, JSON sai, dòng quá 1 MiB, database rollback, restart, mốc end time, PAID/CANCELED sai trạng thái và Seller không được bid. `SocketClientTest` kiểm tra server ngắt khi request đang chờ. Các test dùng SQLite tạm và không phụ thuộc tài khoản cá nhân.

## CI, giới hạn và kết luận

`.github/workflows/ci.yml` cấu hình checkout, Temurin JDK17, `mvn -B clean verify`, upload Surefire/JaCoCo; không phụ thuộc `D:\`. **Chưa có kết quả GitHub Actions thực tế** vì workspace không có `.git`/remote. Không commit hoặc push. Hệ thống đủ điều kiện bàn giao để chạy và demo cục bộ theo [README.md](../README.md); chưa thể tuyên bố hoàn tất 100% tiêu chí lớp học vì B15 cần lịch sử đóng góp thật, vài nhánh GUI cần kiểm tra tay và CI cần chạy trên repository thật. Token chỉ ở bộ nhớ, server mặc định localhost và chưa có TLS; PAID là trạng thái, không phải thanh toán qua cổng.

Rà soát cuối: 8 script PowerShell parse thành công, đúng một `ServerMain` và `ClientMain`, 7 FXML, client POM không có JDBC/SQLite. `data/` không còn database thử; không thấy TODO/FIXME/placeholder trong code bắt buộc. Các `target/`, `_env/`, `data/` được `.gitignore` loại khỏi Git. Không xóa dữ liệu người dùng.
