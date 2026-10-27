# Hợp đồng kiến trúc cho 6 prompt

**Mục đích:** Giữ tên module, DTO và quy tắc trao đổi đã thống nhất giữa client/server. Đây là tài liệu tham chiếu cho người sửa code; nếu mới học, đọc [JAVA_BASICS.md](JAVA_BASICS.md) và [API_PROTOCOL.md](API_PROTOCOL.md) trước. Chi tiết tính năng đang chạy ở [PROJECT_STATE.md](PROJECT_STATE.md).

Tài liệu này khóa tên và giao thức giữa các module. Chỉ đổi khi phát hiện lỗi kỹ thuật và cập nhật đồng bộ code, test, README, PROJECT_STATE.md.

## Module và package

- Root artifact: com.btl.auction:auction-system; Java release 17.
- common: com.btl.auction.common.model (Entity, User, Bidder, Seller, Admin, Item, Electronics, Art, Vehicle, ItemFactory, Auction, BidTransaction, AuctionStatus, UserRole, ItemCategory); common.dto; common.protocol.
- server phụ thuộc common; com.btl.auction.server.ServerMain, server.db (Database, DatabaseInitializer), server.dao, server.service (AuthService, ProductService, AuctionService, AuctionManager), server.network, server.event.
- client phụ thuộc common; com.btl.auction.client.ClientMain, client.network, client.controller, client.view. Client không phụ thuộc server hoặc JDBC.
- Package tests trùng package code tương ứng. Chỉ server truy cập SQLite.

## Mô hình dữ liệu

- ID trong model là long dương. Tiền là long số nguyên VND không âm, không dùng double/float. Bid mới phải lớn hơn giá hiện tại. Giá hiện tại ban đầu là giá khởi điểm.
- User có id, username, displayName, role; mật khẩu và password hash chỉ ở server/DAO. Không gửi hash cho client.
- Item có id, sellerId, name, description, startingPriceVnd và category. Loại cụ thể thêm manufacturer (Electronics), artist (Art), make (Vehicle). `ItemFactory` là creator trừu tượng; `ElectronicsFactory`, `ArtFactory`, `VehicleFactory` ghi đè `create`. `ItemCategory.factory()` trả về creator tương ứng.
- Auction có id, itemId, startAtEpochMs, endAtEpochMs, currentPriceVnd, leaderUserId nullable, status. start < end. Mỗi Item có tối đa một Auction ở schema nền tảng.
- BidTransaction có id, auctionId, bidderUserId, amountVnd, createdAtEpochMs. Cần sắp xếp theo createdAtEpochMs rồi id khi hiển thị.
- Enum: UserRole {BIDDER, SELLER, ADMIN}; ItemCategory {ELECTRONICS, ART, VEHICLE}; AuctionStatus {OPEN, RUNNING, FINISHED, PAID, CANCELED}.
- Chuyển trạng thái: OPEN → RUNNING → FINISHED → PAID hoặc CANCELED. Server áp dụng theo thời gian và giao dịch. Không bid khi trạng thái khác RUNNING hoặc now < start / now >= end.
- Mốc lưu trữ/giao thức: epoch milliseconds UTC dưới dạng số nguyên JSON. GUI dùng ZoneId.systemDefault() chỉ lúc hiển thị. Server dùng Clock để test thời gian.

## Giao thức TCP

Mỗi message là một JSON object UTF-8 trên đúng một dòng TCP, kết thúc LF; không nhúng LF thô trong chuỗi. Một kết nối có thể mang nhiều request và event. Giới hạn dòng 1 MiB; vượt giới hạn trả lỗi rồi đóng kết nối. Gson dùng serializeNulls; payload là object.

Request: { "requestId": "uuid/string", "type": "PING", "token": null, "payload": {} }.
Response thành công: { "requestId": "same-id", "success": true, "payload": {}, "errorCode": null, "message": null }.
Response lỗi: { "requestId": "same-id", "success": false, "payload": null, "errorCode": "VALIDATION_ERROR", "message": "..." }.
Event: { "type": "BID_UPDATED", "auctionId": 42, "payload": {} }. Event không có requestId và không phải response.

- RequestType: PING, REGISTER, LOGIN, LOGOUT, LIST_AUCTIONS/GET_AUCTIONS, GET_AUCTION/GET_AUCTION_DETAIL, GET_MY_ITEMS, LIST_ITEMS, GET_ITEM, CREATE_ITEM, UPDATE_ITEM, DELETE_ITEM, CREATE_AUCTION, PLACE_BID, GET_BIDS/GET_BID_HISTORY, SUBSCRIBE_AUCTION, UNSUBSCRIBE_AUCTION, MARK_PAID, CANCEL_AUCTION. Tên trước dấu `/` là alias Prompt 1; tên Prompt 3 là tên khuyến nghị. Server hiện dispatch toàn bộ các type này. Xem API_PROTOCOL.md.
- EventType: BID_UPDATED, AUCTION_STARTED, AUCTION_FINISHED, AUCTION_EXTENDED. Prompt 2 đổi BID_PLACED thành BID_UPDATED để khớp tên event bắt buộc; code, test và consumer Prompt 3 dùng tên mới.

Prompt 5 bổ sung SET_AUTO_BID/GET_AUTO_BID/REMOVE_AUTO_BID (actor lấy từ session token), DTO `AutoBidDto` và event `AUCTION_EXTENDED` có AuctionDto chứa end time mới. `PLACE_BID` vẫn trả bid thủ công cùng trạng thái phiên cuối sau auto-bid; auto-bid đã thực hiện được phát qua `BID_UPDATED`. Chi tiết payload ở `API_PROTOCOL.md`.
- ErrorCode: INVALID_REQUEST, UNSUPPORTED_REQUEST, VALIDATION_ERROR, AUTH_REQUIRED, AUTH_FAILED, FORBIDDEN, NOT_FOUND, AUCTION_NOT_RUNNING, BID_TOO_LOW, CONFLICT, INTERNAL_ERROR.
- Không gửi stack trace hoặc hash mật khẩu. Request lỗi cú pháp có requestId null. Server không tự trả success cho type chưa triển khai.
- Session token là chuỗi ngẫu nhiên đủ mạnh do server cấp sau LOGIN; token ở bộ nhớ server, logout/timeout thu hồi. Không đặt token trong event hay log. REGISTER/LOGIN/PING không cần token.

## Payload

| Type | Request payload | Success payload |
| --- | --- | --- |
| PING | {} | { "pong": true } |
| REGISTER | username, displayName, password, role (BIDDER/SELLER) | user: UserDto |
| LOGIN | username, password | token, user: UserDto |
| LOGOUT | {} | {} |
| GET_AUCTIONS (alias LIST_AUCTIONS) | status? | auctions: AuctionDto[] |
| GET_AUCTION_DETAIL (alias GET_AUCTION) | auctionId | auction: AuctionDto |
| GET_MY_ITEMS | {} | items: ItemDto[] của Seller đăng nhập |
| LIST_ITEMS | {} | items: ItemDto[] cho Admin |
| GET_ITEM | itemId | item: ItemDto |
| CREATE_ITEM | category, name, description, startingPriceVnd, detail | item: ItemDto |
| UPDATE_ITEM | itemId, name, description, startingPriceVnd, detail | item: ItemDto |
| DELETE_ITEM | itemId | {} |
| CREATE_AUCTION | itemId, startAtEpochMs, endAtEpochMs | auction: AuctionDto |
| PLACE_BID | auctionId, amountVnd | bid: BidDto, auction: AuctionDto |
| GET_BID_HISTORY (alias GET_BIDS) | auctionId | bids: BidDto[] |
| SUBSCRIBE_AUCTION | auctionId | auction: AuctionDto; nhận event sau đó |
| UNSUBSCRIBE_AUCTION | auctionId | {} |
| MARK_PAID/CANCEL_AUCTION | auctionId | auction: AuctionDto |

UserDto: id, username, displayName, role. ItemDto: id, sellerId, category, name, description, startingPriceVnd, detail. AuctionDto: id, itemId, startAtEpochMs, endAtEpochMs, currentPriceVnd, leaderUserId, status. BidDto: id, auctionId, bidderUserId, amountVnd, createdAtEpochMs. Các DTO là ảnh chụp dữ liệu; server là nguồn sự thật. Bid event chứa auction: AuctionDto và bid: BidDto; finish event chứa auction: AuctionDto. Prompt 3 thêm subscription theo auction; client bỏ qua snapshot cũ khi giá/trạng thái đã tiến lên.

## Service và transaction

- AuthService.register(username, displayName, password, role), login(username, password), logout(token), requireUser(token).
- ProductService.createItem(sellerId, ...), updateItem(sellerId, itemId, ...), deleteItem(sellerId, itemId), listItems(...).
- AuctionService.createAuction(sellerId, itemId, start, end), placeBid(bidderId, auctionId, amountVnd), closeExpired(now), markPaid(actorId, auctionId), cancel(actorId, auctionId), getAuction(id), listAuctions(...).
- AuctionManager là Singleton quản lý khóa riêng cho từng auction; không giữ bản sao authoritative của giá. Observer được gọi sau commit thành công, tránh phát event cho transaction rollback. Socket broadcaster gửi tới các client theo dõi auction; lỗi gửi không rollback bid đã commit.
- Bid: khóa theo auction, mở transaction JDBC, đọc giá/trạng thái hiện tại, kiểm tra thời gian/quyền/giá, ghi bids và cập nhật auctions, commit, rồi notify. Dùng thứ tự lock cố định nếu cần nhiều auction. Kết thúc phiên dùng cùng khóa và transaction. SQLite bật foreign_keys và busy_timeout cho từng connection. Một auction chỉ có một leader.

## Database

Schema ở server/src/main/resources/db/schema.sql. Bảng users(id PK, username UNIQUE, display_name, password_hash, role, created_at_ms); items(id PK, seller_id FK users, category, name, description, starting_price_vnd, detail_json); auctions(id PK, item_id UNIQUE FK items, start_at_ms, end_at_ms, current_price_vnd, leader_user_id FK users nullable, status); bids(id PK, auction_id FK auctions, bidder_user_id FK users, amount_vnd, created_at_ms). CHECK cho tiền/thời gian/enum và index cho truy vấn thường dùng. Schema tạo bằng CREATE TABLE IF NOT EXISTS; thay đổi schema về sau phải dùng migration có phiên bản, không sửa ngầm database cũ.

## Build, run, chất lượng

- Từ root: mvn -B clean verify.
- Trên máy Windows của nhóm: chạy scripts/Use-LocalConda.ps1 trước; JDK 17, Maven và Maven repository nằm trong _env. Dùng mvn -o -B clean verify để kiểm tra đủ dependency offline. environment.yml ghi phiên bản Conda.
- Cài artifact nội bộ trước khi chạy module riêng: mvn -B install -DskipTests.
- Server: scripts/run-server.ps1 hoặc mvn -pl server exec:java -Dexec.mainClass=com.btl.auction.server.ServerMain. Mặc định chỉ bind 127.0.0.1:5555, DB data/auction.db; tham số lần lượt là port và path DB.
- Client: `scripts/run-client.ps1` hoặc `mvn -pl client javafx:run` sau khi cài artifact nội bộ; mở GUI bảy màn JavaFX/FXML.
- Checkstyle kiểm tra quy tắc nền; JaCoCo ghi báo cáo coverage. CI chạy mvn -B clean verify trên JDK 17. Mục tiêu ≥60% coverage logic quan trọng trước giai đoạn tích hợp.
- Không dùng Spring Boot, Hibernate, Redis, Docker, microservices. Không tự tạo Git commit trong các prompt.
