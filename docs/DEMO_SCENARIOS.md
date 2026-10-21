# Kịch bản demo và nghiệm thu

**Mục đích:** Chuẩn bị buổi trình diễn 10–15 phút sau khi đã chạy được ứng dụng theo [GETTING_STARTED.md](GETTING_STARTED.md). Cần Windows desktop, server và nhiều cửa sổ client. Dữ liệu demo chỉ phục vụ thử nghiệm local.

## Chuẩn bị

Tại `D:\BTL_LTNC`, chạy `scripts/run-tests.ps1`; mở PowerShell mới chạy `scripts/run-demo.ps1` để seed `data/demo.db` và mở server `127.0.0.1:5555`. Mở hai hoặc ba PowerShell khác, mỗi cửa sổ chạy `scripts/run-client.ps1`. Tài khoản `demo_admin`, `demo_seller`, `demo_bidder_a`, `demo_bidder_b`; mật khẩu local mặc định `DemoPass123!` (có thể đổi bằng `AUCTION_DEMO_PASSWORD` trước khi seed). Seed lại không tạo trùng; phiên RUNNING ban đầu có hạn khoảng 30 phút từ lần seed đầu, nên dùng Seller tạo phiên mới nếu đã hết hạn.

## Demo GUI 10–15 phút

1. Đăng nhập Seller, xem ba sản phẩm Electronics/Art/Vehicle và các phiên demo. Tạo sản phẩm mới, sửa/xóa một sản phẩm chưa có phiên; tạo phiên từ sản phẩm mới.
2. Hai Bidder đăng nhập ở hai cửa sổ, mở cùng phiên. A đặt giá, B quan sát giá/người dẫn đầu và biểu đồ tăng; B đặt cao hơn, A nhận event. Thử giá bằng, âm và role sai để xem lỗi.
3. Đặt auto-bid max/increment ở A, cho B bid cạnh tranh; quan sát giá không vượt max. Nếu dùng cửa sổ cuối của phiên, quan sát `AUCTION_EXTENDED` và countdown mới. Chờ FINISHED, xem winner và giá cuối.
4. Đăng nhập Admin ở cửa sổ khác để xem quản lý; Seller/Admin đánh dấu PAID hoặc CANCEL với phiên phù hợp. Logout rồi login lại. Tắt server và quan sát client báo mất kết nối, sau đó khởi động lại và đối chiếu dữ liệu.

## Bằng chứng tự động

`SocketServerIntegrationTest.finalEndToEndFlowSurvivesServerRestartAndKeepsWinner` dùng TCP/JSON thật: Seller đăng nhập/tạo item/auction, hai Bidder lấy chi tiết và subscribe, bid hai chiều, bid thấp bị từ chối, hai bid đồng thời, nhận FINISHED, chặn bid muộn, restart server và so khớp SQLite. Chạy riêng cổng 5555 bằng Maven với `-Dauction.e2e.port=5555` sau khi dừng server demo. `GuiFlowIT` qua `scripts/test-gui.ps1` mở ba Stage JavaFX và click luồng cơ bản; test GUI nâng cao/nhánh Admin vẫn có checklist thủ công ở [GUI_GUIDE.md](GUI_GUIDE.md).

## Khi thuyết trình

Người 1 giải thích `AuctionService`/concurrency, người 2 trình diễn GUI/MVC, người 3 giải thích TCP/Observer, người 4 trình bày SQLite/DAO/CI. Mỗi người cần trả lời được luồng đầy đủ và minh họa bằng [ARCHITECTURE.md](ARCHITECTURE.md), [CODE_WALKTHROUGH.md](CODE_WALKTHROUGH.md), [DEFENSE_QUESTIONS.md](DEFENSE_QUESTIONS.md).
