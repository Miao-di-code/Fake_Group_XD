# Backend: service, quyền và giao dịch

**Mục đích:** Tra cứu các service và quy tắc xử lý trên server. Nếu mới học Java, đọc [JAVA_BASICS.md](JAVA_BASICS.md), [ARCHITECTURE.md](ARCHITECTURE.md) và [CODE_WALKTHROUGH.md](CODE_WALKTHROUGH.md) trước. Đường dẫn source là `server/src/main/java/com/btl/auction/server/`.

## Chạy và cấu hình

Từ `D:\BTL_LTNC` trong PowerShell:

```powershell
. .\scripts\Use-LocalConda.ps1
mvn -o -B clean verify
```

JDK 17, Maven và Maven repository nằm trong `_env`. SQLite là nguồn dữ liệu chính. `ServerMain` khởi tạo DB và `AuctionScheduler`, quét trạng thái mỗi giây. TCP request routing và realtime ở [API_PROTOCOL.md](API_PROTOCOL.md). Các bước chạy đầy đủ ở [GETTING_STARTED.md](GETTING_STARTED.md).

Tạo Admin local một lần bằng `AdminBootstrap`, truyền mật khẩu qua biến môi trường trong phiên PowerShell cá nhân; không truyền trên dòng lệnh và không commit biến này:

```powershell
$env:AUCTION_ADMIN_PASSWORD = (Get-Credential -UserName admin).GetNetworkCredential().Password
mvn -o -B install -DskipTests
mvn -o -pl server exec:java -Dexec.mainClass=com.btl.auction.server.service.AdminBootstrap -Dexec.args='data/auction.db admin DemoAdmin'
Remove-Item Env:AUCTION_ADMIN_PASSWORD
```

Đăng ký thường không nhận vai trò ADMIN. AdminBootstrap chỉ gọi API tạo Admin có phạm vi package; không có route TCP cho việc này.

## Service API

Các method dùng ID actor lấy từ `AuthService.requireUser(token).id()` trong tầng request. Không đọc actor ID từ payload client. `ServiceException.getCode()` ánh xạ sang `ErrorCode` của protocol; thông báo nội bộ/database không gửi nguyên stack trace cho client.

| Service | Public API | Quy tắc |
| --- | --- | --- |
| AuthService | `register(username, displayName, password, role)`, `login(username, password)`, `logout(token)`, `requireUser(token)`, `requireRole(token, role)` | Chỉ BIDDER/SELLER đăng ký; PBKDF2 HMAC SHA256, salt ngẫu nhiên; session 8 giờ trong bộ nhớ |
| ProductService | `createItem(actorId, category, name, description, startingPriceVnd, detail)`, `updateItem(actorId, itemId, name, description, startingPriceVnd, detail)`, `deleteItem(actorId, itemId)`, `getItem(itemId)`, `listItems(sellerId)`, `listOwnItems(actorId)` | Seller tạo/quản lý sản phẩm mình; Admin sửa/xóa; item đã có auction không sửa/xóa |
| AuctionService | `createAuction(sellerId, itemId, start, end)`, `getAuction(id)`, `listAuctions(status)`, `getBids(auctionId)`, `placeBid(bidderId, auctionId, amountVnd)`, `closeExpired(now)`, `markPaid(actorId, auctionId)`, `cancel(actorId, auctionId)` | Seller tạo cho item mình; Bidder bid; Seller chủ sở hữu/Admin hoàn tất sau FINISHED |
| SnapshotService | `exportSnapshot(path)`, `restoreToNewDatabase(snapshotPath, destination)` | Snapshot v2 chứa 5 bảng, gồm `auto_bids` và password hash; restore chỉ tới DB chưa tồn tại |
| AuctionScheduler | constructor `(AuctionService, Clock)`, `close()` | Tự quét mỗi giây trong ServerMain; Clock inject được cho test |

`AuthService.login()` trả `LoginResult(token, user)`. `AuctionService.placeBid()` trả `BidResult(bid, auction)`. List trạng thái nhận `null` để lấy tất cả; `listItems(null)` tương tự. `getAuction` và `listAuctions` đồng bộ trạng thái theo Clock trước khi trả.

## Quy tắc bid và event

- Giá hiện tại ban đầu bằng giá khởi điểm. Bid phải **lớn hơn** giá hiện tại; dùng `long` VND. Giá bằng hoặc thấp hơn bị từ chối; không đặt bước giá tối thiểu khác.
- Chỉ BIDDER được bid khi trạng thái RUNNING và `start <= now < end`. Tại `now == end`, bid bị từ chối. Không có bid thì `leaderUserId == null` ngay cả khi FINISHED; không thể PAID, có thể CANCELED.
- `AuctionManager` khóa từng auction. Trong khóa, SQLite transaction ghi bid và cập nhật giá/leader bằng điều kiện giá cũ; lỗi làm rollback cả hai. DB là nguồn giá duy nhất. Event được xếp hàng theo từng auction sau commit; hàng đợi được xử lý **sau khi nhả khóa**, giữ đúng thứ tự giá khi listener chậm. Lỗi listener được ghi log, không đảo ngược giao dịch đã commit.
- FINISHED chuyển PAID hoặc CANCELED do Seller sở hữu hoặc Admin thực hiện; PAID cần có winner. Không có thanh toán thực.
- Snapshot chứa password hash nên bảo vệ file như bản sao DB. Export từ chối đường dẫn của DB đang dùng. Restore không ghi đè DB đang chạy; nhập vào file tạm, kiểm tra khóa ngoại, rồi chuyển đến đường dẫn đích mới.

Event bid trong code là `BID_UPDATED`; `BID_PLACED` là tên cũ không dùng nữa. `AUCTION_EXTENDED` được phát khi bid hợp lệ ở cửa sổ cuối phiên; chính sách cấu hình qua `AntiSnipingPolicy` và server mặc định 30/30 giây.

## Tích hợp networking

Request router khởi tạo một bộ `AuthService`, `ProductService`, `AuctionService` với cùng `Database` và `Clock`. REGISTER/LOGIN gọi AuthService; các request cần quyền trước hết gọi `requireUser(token)` rồi truyền `user.id()` cho service. `ServiceException.code` được chuyển thành error response. Socket broadcaster đăng ký `AuctionListener` theo auction, serialize `EventMessage`, ghi socket ngoài lock và quản lý subscriber/disconnect.

**Ghi nhớ:** `RequestDispatcher` xác định người gọi, service quyết định quy tắc, DAO ghi/đọc SQL. Hãy chạy [các test backend](TEST_CASES.md) trước khi sửa quy tắc giá.
