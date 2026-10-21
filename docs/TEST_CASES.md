# Bản đồ các bài kiểm thử

**Mục đích:** Biết mỗi test bảo vệ quy tắc nào. **Unit test** kiểm một phần nhỏ như `AutoBidCalculator`; **integration test** kiểm nhiều phần cùng làm việc, như service + SQLite hoặc nhiều socket. **Test case** là tình huống có dữ liệu vào và kết quả mong đợi. **JUnit 5** là thư viện chạy các method test Java. Đọc [GETTING_STARTED.md](GETTING_STARTED.md) để chạy test; `src/test/java` của mỗi module chứa mã test.

Chạy `mvn -o -B clean verify` sau `. .\scripts\Use-LocalConda.ps1` tại root. JUnit 5 dùng SQLite tạm (`@TempDir`) và Clock điều khiển được, không phụ thuộc thời gian chờ dài hay dữ liệu thật.

| Test | Mục tiêu |
| --- | --- |
| `registrationLoginSessionAndAdminRules` | Đăng ký, trùng username, chặn Admin, validation, sai mật khẩu, role, logout, hết hạn, hash không chứa plaintext |
| `productOwnershipAndAuctionProtection` | CRUD Seller/Admin, chặn Seller khác, chặn sửa/xóa item đã có phiên |
| `biddingTransitionsWinnerAndFinalization` | OPEN → RUNNING → FINISHED → PAID, giá bằng bị từ chối, bid hợp lệ, leader, lịch sử, event, chính xác tại end |
| `noBidMeansNoWinnerAndCancellation` | OPEN đã quá end tự chuyển qua FINISHED, không tự tạo winner, không PAID, CANCELED hợp lệ |
| `concurrentBidsHaveOneConsistentLeaderAndNoLostHistory` | 2 bidder tranh nhau, giá/leader cuối nhất quán, lịch sử không mất |
| `manyThreadsDoNotLoseAcceptedBids` | 24 tác vụ đồng thời; số bid lưu bằng số bid thành công, giá cao nhất đúng |
| `databaseFailureRollsBackBidAndDoesNotPublish` | Trigger SQLite ép update lỗi: không có bid giả, giá cũ giữ nguyên, không event |
| `snapshotRoundTripKeepsRelatedRowsAndRejectsOverwrite` | Export/restore users/items/auctions/bids, giữ giá, không ghi đè DB đích |
| `observerDoesNotHoldAuctionLock` | Listener chậm không cản bid kế tiếp vào cùng auction |
| `unauthorizedAuctionActionsAndDatabaseReopen` | Chặn tạo/hoàn tất không có quyền, chặn Seller bid, dữ liệu bền sau mở DB lại |

Các test nền tảng Prompt 1 vẫn chạy cho model, JSON protocol, schema, event publisher và AuctionManager. Kết quả build cuối được ghi tại PROJECT_STATE.md.

## Mạng và GUI

| Test | Mục tiêu |
| --- | --- |
| `SocketServerIntegrationTest` | Seller tạo item/auction; hai Bidder subscribe và nhận BID_UPDATED/AUCTION_FINISHED; bid đồng thời; quyền Admin; JSON lỗi và ngắt socket |
| `SocketClientTest` | Ghép response theo requestId dù đảo thứ tự; bỏ snapshot giá cũ; báo ngắt kết nối và thất bại future đang chờ |
| `FxmlContractTest` | Kiểm tra bảy FXML, controller, fx:id, onAction và CSS; trên Windows có desktop còn tải tất cả FXML bằng JavaFX toolkit |
| `GuiFlowIT` (chạy riêng) | Ba cửa sổ JavaFX với server thật: đăng ký/đăng nhập Seller + hai Bidder, tạo item/auction, bid hai chiều, giá realtime, FINISHED và màn kết quả |

Chạy `GuiFlowIT` bằng `scripts/test-gui.ps1` khi server tạm đang chạy; test này không nằm trong `clean verify` mặc định vì cần desktop, server thật và thời gian chờ kết thúc phiên. Các nhánh GUI còn cần thử thủ công được liệt kê ở `GUI_GUIDE.md`.

## Auto-bid, gia hạn và lưu trữ

| Test | Kiểm chứng |
| --- | --- |
| `AutoBidCalculatorTest` | Max cap, một bước trả giá, hòa ưu tiên theo thời gian/ID, không tự đấu vô hạn |
| `AntiSnipingPolicyTest` | Mốc `end-X`, ngay trước end, tại end và cấu hình tắt |
| `AdvancedFeaturesIntegrationTest` | Quyền, lịch sử và cap auto-bid; hòa max; restart/snapshot v1-v2; extension + scheduler; bid thường đồng thời với auto-bid |
| `DatabaseInitializerTest.upgradesExistingV1DatabaseWithoutLosingRows` | Nâng DB cũ và giữ user hiện có |
| `SocketServerIntegrationTest.autoBidAndAntiSnipingBroadcastUseRealSocketAndCurrentEnd` | Request mới, BID_UPDATED và AUCTION_EXTENDED qua hai socket thật |
| `SocketClientTest` | End time mới không bị bỏ khi giá không đổi; event end time cũ không ghi đè |

`mvn -B clean verify` chạy JUnit, Checkstyle và ngưỡng JaCoCo line coverage 60% cho module server. Trên máy này dùng `scripts/Use-LocalConda.ps1` rồi `mvn -o -B clean verify` vì dependency đã nằm trong `_env`.

## Nghiệm thu cuối

| Test | Kiểm chứng |
| --- | --- |
| `DemoSeederTest` | Bốn tài khoản/ba sản phẩm/ba phiên, một RUNNING/hai OPEN, hash và chạy seed lặp không trùng |
| `SocketServerIntegrationTest.finalEndToEndFlowSurvivesServerRestartAndKeepsWinner` | TCP JSON thật, Seller + hai Bidder, event hai chiều, bid thấp, đồng thời, FINISHED, bid muộn, restart và đọc lại SQLite; chạy riêng cổng 5555 |
| `GuiFlowIT` chạy riêng | Ba Stage JavaFX/server thật, login/register, Seller tạo item/auction, bid realtime, auto-bid form, LineChart tăng điểm, FINISHED/winner |
| `GuiAdminIT` chạy riêng với `scripts/test-gui.ps1 -Admin` | DB demo đã seed, password sai, Admin xem item/auction, quay lại và logout |
| `BackendIntegrationTest.biddingTransitionsWinnerAndFinalization` | Bổ sung giá âm, MARK_PAID trước FINISHED và MARK_PAID lặp sau PAID |

Kết quả cuối và giới hạn kiểm tra tay ở `FINAL_ACCEPTANCE_REPORT.md`.

**Đọc kết quả:** `mvn -B clean verify` hoặc `./scripts/run-tests.ps1` in số tests/failures/errors/skipped và `BUILD SUCCESS`/`BUILD FAILURE`. `*/target/surefire-reports` có kết quả chi tiết. JaCoCo đo tỷ lệ dòng code được test chạy qua, báo cáo tại `*/target/site/jacoco`; tỷ lệ cao không tự chứng minh nghiệp vụ đúng. Checkstyle kiểm tra quy tắc mã nguồn. **CI** là việc tự build/test sau push hoặc PR; workflow `.github/workflows/ci.yml` chạy `mvn -B clean verify` trên GitHub Actions. **CD** là tự triển khai sau CI và chưa có trong dự án; kết quả cục bộ không thay thế kết quả GitHub.

**Nếu lỗi:** Đọc tên test đầu tiên thất bại, mở mã test để thấy dữ liệu vào/kỳ vọng, rồi lần theo class tương ứng từ [CODE_WALKTHROUGH.md](CODE_WALKTHROUGH.md). Test GUI `GuiFlowIT` và `GuiAdminIT` cần desktop/server thật nên chạy riêng theo [GUI_GUIDE.md](GUI_GUIDE.md). **Ghi nhớ:** Số liệu đã đo ở [TEST_REPORT.md](TEST_REPORT.md) và [FINAL_ACCEPTANCE_REPORT.md](FINAL_ACCEPTANCE_REPORT.md) là lịch sử; lần chạy trên máy bạn có thể khác.
