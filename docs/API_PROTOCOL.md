# Giao thức TCP JSON và cách client kết nối

**Mục đích:** Cho biết client/server trao đổi dữ liệu ra sao và API mà JavaFX dùng. Cần đọc [kiến trúc](ARCHITECTURE.md) trước. **Socket** là đầu nối để hai chương trình trao đổi dữ liệu qua mạng; **TCP** giữ một kết nối để server có thể gửi cập nhật ngay. **JSON** là văn bản có cặp tên–giá trị, ví dụ `{"auctionId":1}`. `common.protocol` chứa khuôn thông điệp, `server.network` nhận/xử lý, `client.network.SocketClient` gửi/đọc.

Server lắng nghe `127.0.0.1:5555` mặc định. Mỗi message là **một JSON object UTF-8 trên một dòng**, kết thúc LF; tối đa 1 MiB mỗi dòng. Một kết nối có thể gửi nhiều request; response và event có thể xen kẽ.

## Envelope

```json
{"requestId":"unique-id","type":"GET_AUCTIONS","token":null,"payload":{}}
```

Response thành công: `{"requestId":"unique-id","success":true,"payload":{},"errorCode":null,"message":null}`. Response lỗi: `{"requestId":"unique-id","success":false,"payload":null,"errorCode":"VALIDATION_ERROR","message":"..."}`. JSON hỏng hoặc dòng quá dài nhận `INVALID_REQUEST` với `requestId:null`; dòng quá dài làm đóng kết nối sau response. Event không có `requestId`: `{"type":"BID_UPDATED","auctionId":1,"payload":{"auction":{},"bid":{}}}`.

Client dùng `requestId` để ghép response. Không giả định response đến trước event của cùng một tác vụ. `SocketClient` giữ bảng future theo ID và đọc trên thread nền.

## Request

`token` là token do LOGIN cấp. Những request cần actor dùng user ID từ token ở server; mọi `userId`, `sellerId`, `bidderUserId` hoặc `role` dư trong payload **không quyết định quyền**.

| Type | Auth | Payload | Success payload |
| --- | --- | --- | --- |
| PING | Không | `{}` | `pong:true` |
| REGISTER | Không | `username`, `displayName`, `password`, `role` BIDDER/SELLER | `user:UserDto` |
| LOGIN | Không | `username`, `password` | `token`, `user:UserDto` |
| LOGOUT | Có | `{}` | `{}`; bỏ subscription của socket |
| GET_AUCTIONS | Không | `status?` | `auctions:AuctionDto[]` |
| GET_AUCTION_DETAIL | Không | `auctionId` | `auction:AuctionDto` |
| GET_MY_ITEMS | Seller | `{}` | `items:ItemDto[]` |
| LIST_ITEMS | Admin | `{}` | `items:ItemDto[]` |
| GET_ITEM | Không | `itemId` | `item:ItemDto` |
| CREATE_ITEM | Seller | `category`, `name`, `description`, `startingPriceVnd`, `detail` | `item:ItemDto` |
| UPDATE_ITEM | Seller chủ sở hữu/Admin | `itemId`, `name`, `description`, `startingPriceVnd`, `detail` | `item:ItemDto` |
| DELETE_ITEM | Seller chủ sở hữu/Admin | `itemId` | `{}` |
| CREATE_AUCTION | Seller chủ sở hữu | `itemId`, `startAtEpochMs`, `endAtEpochMs` | `auction:AuctionDto` |
| PLACE_BID | Bidder | `auctionId`, `amountVnd` | `bid:BidDto`, `auction:AuctionDto` |
| SET_AUTO_BID | Bidder | `auctionId`, `maxBidVnd`, `incrementVnd` | `autoBid:AutoBidDto`, `auction:AuctionDto` |
| GET_AUTO_BID | Bidder | `auctionId` | `autoBid:AutoBidDto` hoặc `null` |
| REMOVE_AUTO_BID | Bidder | `auctionId` | `{}` |
| GET_BID_HISTORY | Không | `auctionId` | `bids:BidDto[]` |
| SUBSCRIBE_AUCTION | Có | `auctionId` | `auction:AuctionDto` và bắt đầu nhận event |
| UNSUBSCRIBE_AUCTION | Có | `auctionId` | `{}` |
| MARK_PAID | Seller chủ sở hữu/Admin | `auctionId` | `auction:AuctionDto` |
| CANCEL_AUCTION | Seller chủ sở hữu/Admin | `auctionId` | `auction:AuctionDto` |

Tên cũ `LIST_AUCTIONS`, `GET_AUCTION`, `GET_BIDS` vẫn được server chấp nhận như alias của `GET_AUCTIONS`, `GET_AUCTION_DETAIL`, `GET_BID_HISTORY`. Các type `GET_MY_ITEMS`, `LIST_ITEMS`, `GET_ITEM`, `SUBSCRIBE_AUCTION`, `UNSUBSCRIBE_AUCTION` cũng đã có; không xóa enum cũ.

Tiền là số nguyên JSON `long` VND; thời gian là epoch milliseconds UTC. `status` lọc theo OPEN/RUNNING/FINISHED/PAID/CANCELED. `category` là ELECTRONICS/ART/VEHICLE. Server từ chối số thập phân hoặc số dạng chuỗi cho trường số. Bid phải lớn hơn giá hiện tại và trong `[startAtEpochMs, endAtEpochMs)`.

## Realtime và lỗi

Chỉ socket đã `SUBSCRIBE_AUCTION` cho ID tương ứng nhận `BID_UPDATED`, `AUCTION_STARTED`, `AUCTION_FINISHED`. Bid bị từ chối không sinh event. Event chứa `auction:AuctionDto` (BID_UPDATED thêm `bid:BidDto`); không chứa token/hash. Session đóng hoặc unsubscribe sẽ được gỡ khỏi danh sách nhận. Event được xếp theo thứ tự commit của từng auction ở backend; network gửi qua một writer queue của mỗi socket nên JSON không bị trộn.

SocketClient so sánh trạng thái OPEN < RUNNING < FINISHED < PAID/CANCELED và giá hiện tại; event/snapshot cũ hơn không ghi đè dữ liệu mới. Controller lấy `latestAuction(id)` sau callback, không dùng event giá cũ để gán trực tiếp vào UI.

ErrorCode: `INVALID_REQUEST`, `VALIDATION_ERROR`, `AUTH_REQUIRED`, `AUTH_FAILED`, `FORBIDDEN`, `NOT_FOUND`, `AUCTION_NOT_RUNNING`, `BID_TOO_LOW`, `CONFLICT`, `INTERNAL_ERROR`. Lỗi nội bộ trả thông báo chung, không gửi stack trace. `UNSUPPORTED_REQUEST` còn trong enum cho tương thích, nhưng tất cả request type hiện có đều được dispatcher xử lý.

Socket đã subscribe còn nhận `AUCTION_EXTENDED` với `auction:AuctionDto` có `endAtEpochMs` mới. `SET_AUTO_BID` có thể phát `BID_UPDATED` ngay; response PLACE_BID trả bid thủ công và trạng thái phiên cuối sau khi auto-bid xử lý. `GET_AUTO_BID` chỉ trả cấu hình của bidder đang xác thực; các response công khai không lộ mức tối đa. SocketClient giữ end time tăng khi giá không đổi và bỏ snapshot có end time cũ.

## Server phục vụ nhiều client

`SocketServer` sở hữu `AuthService`, `ProductService`, `AuctionService`, `RequestDispatcher`, `AuctionEventBroadcaster` và `AuctionScheduler`. Mỗi kết nối có `ClientHandler` đọc request và `ClientSession` với hàng đợi ghi riêng. Dispatcher lấy user ID từ token, ánh xạ payload vào service và trả `ResponseMessage`. Broadcaster gửi event đến các session đã subscribe phiên; mất kết nối thì gỡ subscription, không làm dừng server.

Dòng JSON tối đa 1 MiB. JSON sai trong giới hạn nhận `INVALID_REQUEST` và kết nối vẫn có thể tiếp tục; dòng quá dài nhận lỗi rồi đóng. Hàng đợi outbound tối đa 256 message mỗi client; client đọc quá chậm làm đầy hàng đợi sẽ bị ngắt. **Realtime** nghĩa là server chủ động đẩy event khi có thay đổi, không yêu cầu client hỏi liên tục. Nhiều bid cùng lúc vẫn đi qua `AuctionService` và transaction SQLite; xem [đường đi của một bid](CODE_WALKTHROUGH.md).

## API cho JavaFX Controller

Đây là các method có trong `client/src/main/java/com/btl/auction/client/network/SocketClient.java`:

```java
SocketClient client = new SocketClient();
client.addEventListener(event -> Platform.runLater(() -> {
    AuctionDto latest = client.latestAuction(event.auctionId());
    // Hiển thị latest trên màn hình.
}));
client.addDisconnectListener(error -> Platform.runLater(() -> {
    // Báo mất kết nối trên màn hình.
}));
CompletableFuture<ResponseMessage> reply = client.connectAsync("127.0.0.1", 5555)
        .thenCompose(ignored -> client.request(
                RequestType.GET_AUCTIONS, null, new JsonObject()));
```

`connectAsync`, `request` và `send(RequestMessage)` trả `CompletableFuture`, tức kết quả sẽ có sau khi mạng phản hồi. `request` tự tạo `requestId`; `isConnected()` cho biết kết nối hiện tại; `close()` ngắt và làm các request còn chờ thất bại. `addEventListener`/`removeEventListener` quản lý callback. Callback chạy trên thread đọc socket, nên controller phải dùng `Platform.runLater` để sửa giao diện. Muốn kết nối lại sau khi mất mạng, tạo `SocketClient` mới.

Sau LOGIN, `AppController` giữ token trong bộ nhớ và truyền cho request cần quyền. `DetailController` gọi `SUBSCRIBE_AUCTION`, dùng auction trong response làm dữ liệu ban đầu, rồi `UNSUBSCRIBE_AUCTION` khi rời màn. `latestAuction(id)` bỏ dữ liệu cũ hơn theo trạng thái/giá/end time. Một response có thể đến sau event, nên màn hình không gán trực tiếp snapshot cũ lên dữ liệu mới.

## Cách chạy và tự kiểm tra mạng

**Mục tiêu:** Kiểm tra nhiều kết nối mà không cần mở GUI. **Thao tác:** Mở PowerShell ở root, bảo đảm `_env` có sẵn rồi chạy:

```powershell
.\scripts\test-network.ps1
```

**Kết quả mong đợi:** Script chạy Maven offline `clean verify`, trong đó `SocketServerIntegrationTest` mở server/các client và database tạm. Nó kiểm tra Seller tạo item/auction, hai Bidder nhận `BID_UPDATED`, kết thúc phiên, quyền Admin, JSON sai, dòng quá dài, ngắt kết nối và bid đồng thời; `SocketClientTest` kiểm tra ghép `requestId`, bỏ snapshot cũ và mất kết nối. **Nếu lỗi:** Kiểm tra `_env`/Maven và đọc báo cáo ở `*/target/surefire-reports`. Test E2E cổng 5555 riêng cần dừng server demo đang chiếm cổng. Hướng dẫn chạy server/client từng bước ở [GETTING_STARTED.md](GETTING_STARTED.md).

**Ghi nhớ:** Request hỏi hoặc ra lệnh; response trả lời theo `requestId`; event báo thay đổi cho người đang theo dõi phiên. Giá cuối cùng do service và database trên server quyết định.
