# Kế hoạch phát triển 6 tuần

**Mục đích:** Giữ kế hoạch chia việc, test và phụ thuộc cho bốn người. Đây là kế hoạch lịch sử của dự án, không phải nhật ký đã hoàn thành. Người mới nên đọc [MODULE_OWNERSHIP.md](MODULE_OWNERSHIP.md) và [GIT_GITHUB_GUIDE.md](GIT_GITHUB_GUIDE.md) trước.

Kế hoạch nội bộ cho 4 người, khoảng 2 commit/người/tuần (8/tuần, 48 dự kiến). Đây không phải lịch sử commit đã thực hiện. Không tự tạo commit, đổi ngày commit, push hoặc merge. HD PDF có lộ trình 9 tuần (tuần 6–14, trình bày tuần 15); nhóm dùng 6 tuần làm mốc thực hiện nén, sau đó dành thời gian lớp còn lại để ôn tập/demo nếu có.

## Tuần 1 – Nền tảng, OOP, giao thức

| Người | Việc | File | Commit message gợi ý | Test | Hoàn thành khi |
| --- | --- | --- | --- | --- | --- |
| 1 | Model Auction, status | common/model/Auction* | feat(core): add auction state model | ModelTest | Chặn chuyển trạng thái sai |
| 1 | BidTransaction và quy tắc giá | common/model/BidTransaction; common/test | test(core): cover bid value rules | common tests | Giá âm/zero bị từ chối |
| 2 | JavaFX Hello World/FXML | client/ClientMain; client/resources/view | feat(ui): open starter window | javafx:run | Cửa sổ mở |
| 2 | Khung MVC, style | client/controller; client/resources | chore(ui): prepare mvc structure | client smoke | View tách controller |
| 3 | JSON envelope, enum | common/protocol | feat(protocol): define request and response | JsonCodecTest | Round-trip UTF-8 |
| 3 | TCP PING | server/ServerMain; server/network | feat(network): accept ping requests | socket smoke | PING trả pong |
| 4 | User/Item/factory | common/model/User*; Item* | feat(model): add user and item hierarchy | ModelTest | Factory tạo đúng loại |
| 4 | Maven/SQLite/CI | pom; server/db; workflow | build: add modules and schema | mvn verify | 4 bảng tạo được, CI cấu hình |

## Tuần 2 – Dữ liệu, xác thực, tạo phiên

| Người | Việc | File | Commit message gợi ý | Test | Hoàn thành khi |
| --- | --- | --- | --- | --- | --- |
| 1 | AuctionService tạo phiên | server/service/AuctionService | feat(core): create auction service | service tests | Mốc giờ/giá hợp lệ |
| 1 | Trạng thái theo Clock | server/service/AuctionService | feat(core): start and close auctions | clock tests | Đúng ranh giới thời gian |
| 2 | Login/Register FXML | client/view; client/controller | feat(ui): add auth screens | GUI smoke | Nhập liệu/hiển thị lỗi |
| 2 | Danh sách phiên | client/view; client/controller | feat(ui): list auctions | GUI smoke | Tải danh sách từ server |
| 3 | Request routing | server/network | feat(network): route request types | routing tests | Type lỗi trả mã rõ |
| 3 | Client socket async | client/network | feat(network): add tcp client | socket tests | UI không bị khóa |
| 4 | UserDao, hash, AuthService | server/dao; server/service/AuthService | feat(auth): register and login | auth tests | Hash không lộ, role đúng |
| 4 | ItemDao, ProductService | server/dao; server/service/ProductService | feat(product): add item crud | DAO tests | Chỉ Seller sở hữu sửa/xóa |

## Tuần 3 – Đấu giá thật, concurrency, GUI chi tiết

| Người | Việc | File | Commit message gợi ý | Test | Hoàn thành khi |
| --- | --- | --- | --- | --- | --- |
| 1 | Đặt giá transaction | server/service/AuctionService; dao | feat(core): place bid atomically | bid integration | Giá/leader cùng commit |
| 1 | AuctionManager khóa theo phiên | server/service/AuctionManager | feat(core): serialize auction updates | concurrency stress | Không lost update |
| 2 | Chi tiết sản phẩm | client/view; client/controller | feat(ui): show auction detail | GUI smoke | Hiển thị giá/giờ đúng |
| 2 | Màn đặt giá | client/view; client/controller | feat(ui): submit bids | GUI smoke | Hiển thị lỗi bid |
| 3 | Multi-client handling | server/network | feat(network): handle concurrent clients | socket stress | Nhiều client ổn định |
| 3 | Observer + broadcast | server/event; server/network | feat(network): broadcast bid events | two-client test | Cả hai nhận bid |
| 4 | AuctionDao/BidDao | server/dao | feat(data): persist auctions and bids | DAO tests | FK, truy vấn ổn |
| 4 | Migration/transaction helper | server/db | feat(data): add transaction support | rollback test | Rollback không phát event |

## Tuần 4 – Kết thúc phiên, GUI quản lý, tích hợp

| Người | Việc | File | Commit message gợi ý | Test | Hoàn thành khi |
| --- | --- | --- | --- | --- | --- |
| 1 | Auto-close/winner | server/service/AuctionService | feat(core): close expired auctions | clock tests | Một winner hoặc không ai |
| 1 | PAID/CANCELED | server/service/AuctionService | feat(core): settle auctions | status tests | Quyền và trạng thái đúng |
| 2 | Seller CRUD | client/view; client/controller | feat(ui): manage seller products | GUI e2e | Thêm/sửa/xóa đúng |
| 2 | Admin view | client/view; client/controller | feat(ui): add admin controls | GUI smoke | Admin thao tác đúng quyền |
| 3 | Event finish/start | server/event; client/network | feat(network): push lifecycle events | two-client test | Cập nhật ngay |
| 3 | Reconnect/error path | client/network | fix(network): handle disconnections | disconnect test | Thông báo rõ, không treo |
| 4 | Export/import JSON | server/service; server/db | feat(data): export and import snapshot | round-trip test | Dữ liệu khớp |
| 4 | Checkstyle, coverage | pom; config; workflow | build: enforce quality checks | mvn verify | CI và report hoạt động |

## Tuần 5 – Kiểm thử hệ thống, hoàn thiện bắt buộc

| Người | Việc | File | Commit message gợi ý | Test | Hoàn thành khi |
| --- | --- | --- | --- | --- | --- |
| 1 | Stress bid/race | server/test/service | test(core): stress simultaneous bids | stress tests | Không rollback giá |
| 1 | Sửa edge cases thời gian | server/service | fix(core): handle auction boundaries | clock tests | Đúng tại start/end |
| 2 | Realtime UI | client/controller; client/view | feat(ui): refresh from events | GUI two-client | Không polling |
| 2 | UX và lỗi | client/view; client/controller | fix(ui): improve error feedback | GUI e2e | Người dùng hiểu lỗi |
| 3 | E2E nhiều client | server/test/network | test(network): cover multi-client flow | e2e test | Bid event đúng thứ tự |
| 3 | Kiểm tra protocol | common/protocol; server/network | fix(protocol): validate payloads | malformed tests | Input lỗi an toàn |
| 4 | DB constraint/rollback | server/db; server/test | test(data): cover constraints | integration tests | Không dữ liệu nửa chừng |
| 4 | CI/coverage ≥60% logic | pom; workflow; tests | test: raise core coverage | mvn verify | Báo cáo đạt mục tiêu |

## Tuần 6 – Nâng cao chọn lọc, tài liệu và demo

| Người | Việc | File | Commit message gợi ý | Test | Hoàn thành khi |
| --- | --- | --- | --- | --- | --- |
| 1 | Auto-bid nếu bắt buộc đã xanh | server/service/AuctionService, AutoBidCalculator; server/dao/AutoBidDao | feat(core): add auto bidding | auto-bid tests | Không vượt maxBid |
| 1 | Anti-sniping nếu kịp | server/service/AuctionService | feat(core): extend late auctions | time tests | Gia hạn đúng X/Y |
| 2 | Biểu đồ giá nếu kịp | client/view; client/controller | feat(ui): show live price chart | GUI two-client | Chart tự cập nhật |
| 2 | Chạy demo GUI | client; docs | docs(ui): add demo walkthrough | manual demo | Mở được từng màn |
| 3 | Nâng ổn định socket | server/network; client/network | fix(network): harden event delivery | stress/e2e | Không mất kết nối thường gặp |
| 3 | Sơ đồ giao thức/demo | docs; README | docs(network): explain protocol | review chéo | Mỗi người giải thích được |
| 4 | README triển khai | README; docs | docs: complete setup guide | clean setup | Máy mới chạy được |
| 4 | Báo cáo test, đóng góp | docs; workflow | docs: prepare test evidence | mvn verify | Có kết quả thật, phân công rõ |

Ưu tiên mọi tuần: hoàn thành chức năng bắt buộc và test trước phần tùy chọn. Mỗi commit thực tế chỉ tạo khi công việc và test tương ứng đã xong; các dòng trên là đề xuất để chia việc đều, không phải yêu cầu làm đúng số commit bằng mọi giá.

## Phụ thuộc cho từng commit gợi ý

Trong mỗi ô dưới đây, vế trước/vế sau ứng với commit thứ nhất/thứ hai của đúng người và tuần ở bảng trên. Nếu phụ thuộc chưa đạt test nghiệm thu của dòng trước, chưa bắt đầu commit phụ thuộc. Đây là kế hoạch, không phải lịch sử Git.

| Tuần | Người 1 | Người 2 | Người 3 | Người 4 |
| --- | --- | --- | --- | --- |
| 1 | Model Auction sau enum status / BidTransaction sau Auction model | JavaFX plugin sau root pom / MVC sau FXML khởi động | JSON envelope sau DTO/model / TCP PING sau envelope | User/Item sau Entity và enum / Maven/SQLite/CI sau module pom |
| 2 | Tạo phiên sau ItemDao / Clock lifecycle sau createAuction | Login/Register sau giao thức auth / List sau GET_AUCTIONS | Routing sau service API / SocketClient sau JSON envelope | Auth sau schema users / ProductService sau ItemFactory và items schema |
| 3 | Bid transaction sau AuctionDao/BidDao / khóa phiên sau placeBid | Detail sau GET_AUCTION_DETAIL / form bid sau PLACE_BID | Multi-client sau ClientHandler / broadcast sau publisher và SUBSCRIBE | Auction/Bid DAO sau schema / transaction helper sau DAO |
| 4 | Auto-close sau lifecycle Clock / PAID/CANCELED sau FINISHED | Seller CRUD sau ProductService / Admin sau auth role | Event finish/start sau broadcaster / reconnect sau SocketClient | Snapshot sau schema/DAO / quality gate sau tests có thực |
| 5 | Stress sau khóa/transaction / edge time sau lifecycle test | Realtime UI sau event protocol / lỗi UI sau response lỗi | E2E sau GUI/socket flow / input validation sau protocol parser | DB rollback sau transaction helper / coverage sau test business |
| 6 | Auto-bid sau build bắt buộc xanh / anti-sniping sau auto-close | Chart sau GET_BID_HISTORY/event / GUI demo sau màn hình ổn định | Socket hardening sau E2E / sơ đồ sau protocol ổn định | README sau script thật / báo cáo sau verify cuối |

Mỗi dòng commit gợi ý ở sáu bảng trên đã có nội dung cụ thể, file, test và tiêu chí nghiệm thu; bảng này thêm phụ thuộc tương ứng. Trọng tâm được rải từ nền tảng tuần 1–2, bid/network tuần 3, lifecycle/GUI tuần 4, stress/CI tuần 5 đến tính năng tùy chọn và bàn giao tuần 6.
