# Đọc mã nguồn theo một lượt đặt giá

**Mục đích:** Tìm các file quan trọng và hiểu khi người dùng nhấn **Đặt giá** thì chương trình chạy qua đâu. Đọc [kiến thức Java](JAVA_BASICS.md) và [kiến trúc](ARCHITECTURE.md) trước; sau đó mở file bằng VS Code và tìm đúng method được nêu.

## Bản đồ thư mục

| Nơi | Vai trò | Mở đầu tiên |
| --- | --- | --- |
| `common/src/main/java/com/btl/auction/common/model` | Object nghiệp vụ và quy tắc đơn giản | `Auction.java`, `ItemFactory.java` |
| `common/.../dto` và `common/.../protocol` | Dữ liệu gửi qua mạng và khuôn JSON | `AuctionDto.java`, `RequestMessage.java`, `EventMessage.java` |
| `server/.../service` | Kiểm tra quyền, giá, thời gian; quyết định kết quả | `AuctionService.java`, `AuthService.java`, `ProductService.java` |
| `server/.../dao` và `server/.../db` | SQL, transaction và tạo database | `AuctionDao.java`, `BidDao.java`, `Transactions.java` |
| `server/.../network` và `server/.../event` | Nhận request và phát event | `SocketServer.java`, `RequestDispatcher.java`, `AuctionEventBroadcaster.java` |
| `client/.../controller`, `client/.../view`, `client/.../network` | Nút bấm, điều hướng và socket | `DetailController.java`, `AppController.java`, `SocketClient.java` |
| `client/src/main/resources/view` | Giao diện FXML/CSS | `detail.fxml`, `app.css` |

Trong bảng, `...` rút gọn phần đường dẫn `src/main/java/com/btl/auction/<module>`. Mở [hướng dẫn module và sơ đồ](ARCHITECTURE.md) nếu muốn xem quan hệ lớp.

## 1. Từ nút bấm đến request

Trong `client/src/main/resources/view/detail.fxml`, nút **Đặt giá** gắn `onAction="#bid"`. `client/src/main/java/com/btl/auction/client/controller/DetailController.java` nhận hành động này, đọc giá người dùng nhập, rồi gửi `PLACE_BID` qua `AppController.request`. `AppController` giữ token đăng nhập trong bộ nhớ và dùng một `SocketClient` cho cửa sổ. `SocketClient.request` tự tạo `requestId`, chuyển request thành JSON, ghi lên TCP. Nó đọc response/event bằng thread riêng để cửa sổ không đứng.

**Thử tự tìm:** Trong VS Code nhấn `Ctrl+Shift+F`, tìm `PLACE_BID`; mở từng kết quả ở client, common và server. Khi chỉ muốn xem kết quả mạng, đọc [API_PROTOCOL.md](API_PROTOCOL.md).

## 2. Server biết ai đang đặt giá

`server/src/main/java/com/btl/auction/server/ServerMain.java` mở server. `SocketServer` nhận các kết nối, `ClientHandler` đọc một JSON trên mỗi dòng, `RequestDispatcher.dispatch` chọn việc theo `RequestType`. Với `PLACE_BID`, dispatcher lấy người dùng từ token bằng `AuthService`, rồi gọi `AuctionService.placeBid(bidderId, auctionId, amountVnd)`. ID hoặc vai trò do client tự khai trong payload không quyết định quyền. `ServiceException` được đổi thành response lỗi có `errorCode`.

**Thử tự tìm:** Tìm `case PLACE_BID` trong `RequestDispatcher.java`. So sánh `token` trong `RequestMessage.java` với `bidderId` mà service nhận.

## 3. AuctionService quyết định giá

`server/src/main/java/com/btl/auction/server/service/AuctionService.java` là class xử lý quy tắc đấu giá. **Service** là lớp đặt quy tắc nghiệp vụ giữa network và nơi lưu dữ liệu. Các method đáng đọc: `createAuction` tạo phiên, `placeBid` nhận giá thủ công, `setAutoBid` lưu mức giá tối đa, `closeExpired` kết thúc phiên đã hết giờ, `markPaid`/`cancel` chuyển trạng thái cuối. Method trả `BidResult(bid, auction)` cho dispatcher để tạo response. `AuctionDto` là ảnh chụp phiên gửi cho client.

Đọc `placeBid` theo đúng thứ tự:

1. `refreshOne` cập nhật trạng thái thời gian nếu cần.
2. `locked(auctionId, ...)` dùng `AuctionManager.withAuctionLock` để hai bid cùng phiên không quyết định trên cùng giá cũ.
3. `Transactions.write` mở giao dịch SQLite. Service kiểm tra vai trò Bidder, trạng thái RUNNING, `start <= now < end` và giá mới lớn hơn giá hiện tại.
4. `recordBid` ghi `bids` qua `BidDao`, rồi `AuctionDao.updatePrice` ghi giá/người dẫn đầu với điều kiện giá cũ. Lỗi làm giao dịch rollback: không còn bid nửa chừng.
5. `resolveAutoBid` có thể thêm tối đa một bid proxy; `extendIfNeeded` có thể gia hạn giờ kết thúc. Cả hai vẫn nằm trong đường xử lý cùng phiên.
6. Sau khi giao dịch thành công, service xếp event; sau khi nhả khóa, `publisher.drain` gửi event. Bid bị từ chối không sinh `BID_UPDATED`.

**Ví dụ:** Giá hiện tại 100.000 VND. Hai Bidder cùng gửi 110.000. Một request được xử lý trước và ghi 110.000; request sau thấy giá mới nên nhận `BID_TOO_LOW`. Chỉ một người dẫn đầu ở giá đó. Nếu database ghi lỗi, transaction trả lại cả giá lẫn lịch sử.

**Tự kiểm tra:** Chạy `./scripts/run-tests.ps1`, tìm `concurrentBidsHaveOneConsistentLeaderAndNoLostHistory` và `manyThreadsDoNotLoseAcceptedBids` trong `BackendIntegrationTest.java`. Đây là test cho tình huống trên; kết quả thực tế xem terminal mới chạy, số liệu lịch sử xem [báo cáo](FINAL_ACCEPTANCE_REPORT.md).

## 4. Dữ liệu được lưu và gửi về

**DAO** là class chỉ tập trung thao tác SQL. `AuctionDao` đọc/ghi bảng `auctions`, `BidDao` làm việc với `bids`; `Database` mở kết nối SQLite. Database là file, mặc định `data/auction.db`. Chỉ server mở file này. Xem bảng và khóa ngoại trong [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md).

`AuctionEventPublisher` là nơi phát sự kiện sau commit. `AuctionEventBroadcaster` chọn những `ClientSession` đã `SUBSCRIBE_AUCTION` cùng ID. Mỗi session có hàng đợi ghi riêng, tránh trộn JSON khi response và event cùng đến. `SocketClient` đọc response, ghép bằng `requestId`, còn event được chuyển đến listener. `DetailController` dùng `Platform.runLater` để sửa JavaFX trên đúng thread; `latestAuction(id)` giữ dữ liệu mới hơn, tránh giá cũ ghi đè giá mới. Biểu đồ lấy lịch sử gốc từ server và tải lại khi nhận `BID_UPDATED`.

## 5. Những file khác nên đọc tiếp

`AuthService`/`UserDao` quản lý đăng ký, đăng nhập, token và vai trò. `ProductService`/`ItemDao` quản lý sản phẩm và quyền sở hữu. `ItemCategory.factory()` trả về concrete `ItemFactory` để tạo `Electronics`, `Art` hoặc `Vehicle`. `AuctionScheduler` kiểm tra phiên đến giờ bắt đầu/kết thúc; nó đọc end time hiện tại nên không đóng phiên theo giờ cũ sau gia hạn. `SnapshotService` export/import JSON phục vụ sao lưu; SQLite vẫn là nguồn dữ liệu chính. `DemoSeeder` tạo dữ liệu demo. Đọc [BACKEND_GUIDE.md](BACKEND_GUIDE.md) và [ADVANCED_FEATURES.md](ADVANCED_FEATURES.md) sau khi hiểu luồng cơ bản.

**Ghi nhớ:** Bắt đầu từ một nút FXML, theo request đến service, DAO/database, rồi theo response/event quay lại giao diện. Không cần học hết mọi file trước khi chạy thử.
