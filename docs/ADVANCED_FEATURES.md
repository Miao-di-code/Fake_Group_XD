# Tính năng nâng cao: auto-bid, gia hạn, biểu đồ

**Mục đích:** Giải thích những phần đã có trong code sau khi đã hiểu bid thường. Đọc [CODE_WALKTHROUGH.md](CODE_WALKTHROUGH.md) trước. `AutoBidCalculator` và `AntiSnipingPolicy` nằm trong `server/src/main/java/com/btl/auction/server/service`; `DetailController` nằm trong client.

## Auto-bidding

Bidder dùng `SET_AUTO_BID(auctionId,maxBidVnd,incrementVnd)`, `GET_AUTO_BID`, `REMOVE_AUTO_BID`. Cấu hình riêng lưu trong bảng SQLite `auto_bids` (migration V2). Chỉ bidder sở hữu token được đọc/sửa cấu hình của mình. `maxBidVnd` phải lớn hơn giá hiện tại, `incrementVnd` dương.

Sau mỗi bid hợp lệ hoặc khi thêm cấu hình, `AutoBidCalculator` xếp cấu hình theo max giảm dần, rồi thời điểm đăng ký và ID tăng dần. Người có max cao nhất có thể trả tối đa `min(maxBid, max(currentPrice, max của đối thủ kế tiếp) + increment)`. Một lần tính tạo nhiều nhất một bid tự động; không có vòng lặp đấu giá vô hạn. Khi max bằng nhau, cấu hình đăng ký trước ưu tiên. Bid tự động lưu vào lịch sử và phát `BID_UPDATED` sau commit. Giá trần không bị vượt; bid thường cao hơn max có thể giành vị trí dẫn đầu.

## Anti-sniping

Server production mặc định X=30, Y=30 giây; `scripts/run-server.ps1` nhận `-AntiSnipeWindowSeconds` và `-AntiSnipeExtensionSeconds`, hoặc JVM properties `auction.snipe.window.seconds` và `auction.snipe.extension.seconds`. Bid hợp lệ tại `end-X <= now < end` kéo `endAtEpochMs` thành `end+Y`. Bid bị từ chối không gia hạn. Việc ghi giá và end time cùng transaction; sau commit phát `AUCTION_EXTENDED`. Scheduler dùng end time mới. Constructor `AuctionService` ba tham số giữ chính sách tắt để test cũ có thời gian xác định.

## Biểu đồ bid

Màn chi tiết dùng JavaFX `LineChart<Number,Number>`: trục X là giây từ đầu phiên, trục Y là VND. Dữ liệu gốc từ `GET_BID_HISTORY`; `BID_UPDATED` kích hoạt tải lại lịch sử ngay, không polling. Countdown nhận end time mới qua `AUCTION_EXTENDED`. `SocketClient.latestAuction` bỏ snapshot cũ theo trạng thái, giá và end time.

## Giới hạn hiện tại

Auto-bid dùng quy tắc proxy một bước như trên; nó không mô phỏng tuần tự mọi mức trả giá trung gian của các bidder. `GuiFlowIT` đã chạy thao tác lưu/tắt auto-bid và điểm chart qua ba cửa sổ JavaFX/server thật; countdown anti-sniping chỉ được kiểm chứng bằng service/socket tests, cần quan sát trực quan khi demo. CI chỉ được kiểm chứng bằng Maven cục bộ, chưa có kết quả GitHub Actions.

**Tự kiểm tra:** Chạy `./scripts/run-tests.ps1`, xem `AutoBidCalculatorTest`, `AntiSnipingPolicyTest` và `AdvancedFeaturesIntegrationTest`. Nếu test lỗi, đọc tên method và mốc giá/giờ kỳ vọng trong `*/target/surefire-reports`; dùng [TEST_CASES.md](TEST_CASES.md) để tìm mục tiêu từng test. **Ghi nhớ:** Giá trần auto-bid được lưu trên server, một lượt xử lý tạo tối đa một bid proxy; gia hạn thay đổi end time trong DB rồi mới phát event.
