# Phân công 4 thành viên

**Mục đích:** Chia người phụ trách chính và người review theo module. Đọc [ARCHITECTURE.md](ARCHITECTURE.md) trước để hiểu tên package; thao tác Git từng bước ở [GIT_GITHUB_GUIDE.md](GIT_GITHUB_GUIDE.md).

Đây là quyền phụ trách chính để review và phối hợp, không phải giới hạn ai được đọc/sửa code. Mỗi thành viên phải hiểu toàn bộ luồng từ GUI đến database để bảo vệ.

| Người | Trách nhiệm | File/package chính |
| --- | --- | --- |
| 1 – Auction Core | Auction, BidTransaction, trạng thái, quy tắc bid, khóa theo auction, auto-close, winner, test nghiệp vụ | common/model/Auction*, BidTransaction; server/service/AuctionService, AuctionManager; common và server test liên quan auction |
| 2 – Frontend | JavaFX/FXML/CSS, controller, login/register, danh sách/chi tiết, realtime, Seller/Admin | client/src/main/java/com/btl/auction/client/ClientMain, controller, view; client/src/main/resources/view và style; client tests |
| 3 – Networking | TCP server/client, JSON protocol, xử lý nhiều client, Observer broadcast/event, test socket | common/protocol; server/ServerMain, server/network, server/event; client/network; protocol/network tests |
| 4 – Data & Management | User/Item và kế thừa, auth, product, DAO, SQLite, Maven, CI, Checkstyle, JaCoCo | common/model/User*, Item*, ItemFactory; server/db, server/dao, server/service/AuthService, ProductService; pom.xml, module pom, config, .github/workflows |

Quy tắc phối hợp: Người 1 và 4 thống nhất transaction/DAO trước khi tích hợp bid. Người 2 và 3 thống nhất DTO/protocol trước khi nối UI. Mọi thay đổi public DTO, schema hoặc request type phải cập nhật CONTRACTS.md và review chéo. File dùng chung trong common do người có tính năng tương ứng sửa, người 3 kiểm tra tương thích JSON. Không lưu mật khẩu hoặc token vào log.
