# Dữ liệu SQLite và cách lưu

**Mục đích:** Biết dữ liệu đấu giá nằm ở đâu và các bảng liên hệ thế nào. Đọc [JAVA_BASICS.md](JAVA_BASICS.md) và [kiến trúc](ARCHITECTURE.md) trước.

**Database** là nơi lưu dữ liệu để sau khi tắt chương trình vẫn còn. **SQLite** là database nằm trong một file, mặc định `data/auction.db`, do server mở. **Bảng** giống một danh sách các hàng cùng kiểu. **Khóa chính** (`id`) nhận diện một hàng; **khóa ngoại** liên kết hàng ở bảng khác. Ví dụ `items.seller_id` trỏ đến `users.id`, để biết ai bán sản phẩm. Client không mở file SQLite trực tiếp.

Schema nguồn: `server/src/main/resources/db/schema.sql`; migration cho auto-bid ở `server/src/main/resources/db/migrations/V2__auto_bids.sql`. `DatabaseInitializer.initialize(path)` tạo/nâng bảng và index mà không tạo trùng khi chạy lại. Mỗi JDBC connection bật `foreign_keys=ON` và `busy_timeout=5000`.

| Bảng | Cột chính | Quan hệ/ràng buộc |
| --- | --- | --- |
| users | id, username, display_name, password_hash, role, created_at_ms | username UNIQUE; role BIDDER/SELLER/ADMIN; chỉ lưu PBKDF2 hash |
| items | id, seller_id, category, name, description, starting_price_vnd, detail_json | seller_id → users; giá ≥0; category ELECTRONICS/ART/VEHICLE |
| auctions | id, item_id, start_at_ms, end_at_ms, current_price_vnd, leader_user_id, status | item_id UNIQUE → items; leader_user_id nullable → users; end > start; giá ≥0 |
| bids | id, auction_id, bidder_user_id, amount_vnd, created_at_ms | FK đến auctions/users; amount >0; lịch sử sắp theo created_at_ms rồi id |

Các mốc thời gian là epoch milliseconds UTC. `detail_json` hiện chứa chuỗi detail đơn giản theo `ItemDto.detail`; tên cột được giữ theo schema Prompt 1, chưa có cấu trúc JSON con. Không đổi schema ngầm khi DB đã tồn tại; thay đổi tương lai cần migration có phiên bản.

Index: `idx_items_seller`, `idx_auctions_status_end`, `idx_bids_auction_time`. `AuctionDao.updatePrice` dùng điều kiện `current_price_vnd` cũ và RUNNING để chống cập nhật mất giá, bên cạnh khóa per-auction. `bids` và `auctions` được ghi trong cùng transaction.

Snapshot JSON phiên bản 1 chứa toàn bộ hàng của users, items, auctions, bids theo ID, kể cả password hash và khóa ngoại. Restore theo thứ tự này vào DB mới, kiểm tra foreign key trước khi đưa file vào đường dẫn đích. Không có tự động restore vào DB đang hoạt động. Phiên bản hiện hành là v2 như bên dưới.

## Bảng auto_bids và migration v2

`DatabaseInitializer` nâng database hiện có lên `PRAGMA user_version=2` trong transaction. Bảng `auto_bids` lưu một cấu hình mỗi cặp `(auction_id, bidder_user_id)`: `max_bid_vnd`, `increment_vnd`, `created_at_ms`; khóa ngoại liên kết auction/user. UPSERT giữ thời điểm đăng ký ban đầu để phân xử khi max bằng nhau. Snapshot JSON v2 chứa `auto_bids`; restore vẫn đọc snapshot v1.

**Ví dụ:** Bidder 5 đặt auto-bid cho phiên 42: một hàng `auto_bids` liên kết `auction_id=42`, `bidder_user_id=5`. Khi có bid thật, `BidDao` thêm hàng vào `bids`; `AuctionDao` cập nhật giá và người dẫn đầu trong cùng transaction. Nếu một thao tác SQL lỗi, mọi thay đổi của transaction được trả lại. Test tương ứng ở `BackendIntegrationTest` và `AdvancedFeaturesIntegrationTest`.

**Tự kiểm tra:** Chạy `./scripts/run-tests.ps1` từ root; xem `DatabaseInitializerTest` và test snapshot trong báo cáo JUnit. **Nếu lỗi:** Đọc lỗi SQLite đầu tiên trong `*/target/surefire-reports`; không sửa tay DB thật. **Ghi nhớ:** SQLite là nguồn dữ liệu chính, JSON snapshot dùng để export/import sang database mới. Đọc tiếp [BACKEND_GUIDE.md](BACKEND_GUIDE.md).

