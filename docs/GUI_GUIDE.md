# Giao diện JavaFX: màn hình và thao tác

**Mục đích:** Biết màn nào làm gì và tự thử các thao tác chưa được GUI test bao phủ. **JavaFX** là thư viện tạo cửa sổ desktop, **FXML** mô tả nút/ô nhập, **Controller** nhận thao tác. Nếu mới bắt đầu, làm theo [GETTING_STARTED.md](GETTING_STARTED.md) để cài và chạy trước.

## Chạy trên Windows

Mở hai cửa sổ PowerShell tại root `D:\BTL_LTNC`. Cửa sổ thứ nhất chạy `./scripts/run-server.ps1`, cửa sổ thứ hai chạy `./scripts/run-client.ps1`. Mỗi script tự chọn JDK/Maven trong `_env`, cài artifact nội bộ offline rồi chạy đúng module. Server mặc định dùng `127.0.0.1:5555` và `data/auction.db`. Client dùng cùng địa chỉ; `ClientMain` cũng nhận named args `--host=... --port=...` khi cấu hình launcher riêng.

Admin không được tự đăng ký. Tạo Admin local một lần bằng lệnh trong `docs/BACKEND_GUIDE.md`, sau đó đăng nhập như tài khoản thường.

## Màn hình và thao tác

| Màn | Chức năng |
| --- | --- |
| Đăng nhập | LOGIN, nhận token/UserDto, báo lỗi mật khẩu/server offline |
| Đăng ký | REGISTER Bidder hoặc Seller, xác nhận mật khẩu, quay lại đăng nhập |
| Danh sách | GET_AUCTIONS và GET_ITEM để hiển thị tên, loại, giá, trạng thái, hạn; xem chi tiết; mở Seller/Admin theo vai trò; logout |
| Chi tiết | GET_AUCTION_DETAIL, GET_ITEM, GET_BID_HISTORY, SUBSCRIBE_AUCTION; đặt bid; giá/người dẫn đầu/trạng thái/thời gian còn lại cập nhật từ event; kết quả FINISHED/PAID/CANCELED |
| Kết quả | GET_AUCTION_DETAIL và GET_ITEM; trạng thái cuối, giá cuối, winner ID, thông báo thanh toán/hủy; tải lại khi cần |
| Seller | GET_MY_ITEMS; CREATE_ITEM, UPDATE_ITEM, DELETE_ITEM, CREATE_AUCTION; xem trạng thái phiên của mình qua GET_AUCTIONS |
| Admin | LIST_ITEMS, GET_AUCTIONS; xem chi tiết, MARK_PAID, CANCEL_AUCTION |

Tất cả lệnh mạng dùng một `SocketClient` của cửa sổ, bất đồng bộ. `AppController` giữ token trong bộ nhớ, không lưu file. Màn chi tiết đăng ký event khi mở và hủy đăng ký khi rời màn. Callback mạng dùng `Platform.runLater`; giá chính thức chỉ lấy từ response/event server và `latestAuction(id)` loại snapshot cũ. JavaFX chỉ hiển thị user ID công khai ở Seller/người dẫn đầu vì protocol hiện chưa có request tra cứu tên công khai. `PAID` là dấu trạng thái do Seller chủ sở hữu/Admin đặt, chưa có cổng thanh toán.

## Smoke test GUI tự động với server thật

Chạy server với DB tạm trong một PowerShell: `./scripts/run-server.ps1 -DatabasePath data/gui-smoke.db`. Từ PowerShell thứ hai chạy `./scripts/test-gui.ps1`. `GuiFlowIT` mở ba cửa sổ JavaFX thật, dùng các nút/form để đăng ký Seller và hai Bidder, tạo sản phẩm/phiên, bid luân phiên, xác nhận hai chiều nhận giá realtime, chờ FINISHED và mở màn kết quả. Test không dùng mock SocketClient. Dừng server rồi xóa riêng DB tạm sau khi kiểm tra. Script test mặc định kết nối port 5555; tham số `-Port` cho server/test phải khớp nhau.

## Tự kiểm tra giao diện từng bước

**Bước 1 – Mục tiêu:** Có tài khoản cho các vai trò. **Thao tác:** Khởi động server và ba client theo hướng dẫn chạy; ở màn đăng ký tạo một Seller và hai Bidder, sau đó đăng nhập Seller. Admin dùng dữ liệu demo hoặc `AdminBootstrap` theo [BACKEND_GUIDE.md](BACKEND_GUIDE.md). **Kết quả mong đợi:** Màn danh sách mở, Seller thấy nút **Quản lý sản phẩm**. **Nếu lỗi:** Kiểm tra server đang nghe cổng 5555, mật khẩu đúng và thông báo `AUTH_FAILED` nếu đăng nhập sai.

**Bước 2 – Mục tiêu:** Seller tạo phiên có thể đấu giá. **Thao tác:** Vào **Quản lý sản phẩm**, nhập tên/mô tả/giá/loại, nhấn **Thêm sản phẩm**. Chọn item vừa tạo, thử **Sửa sản phẩm đã chọn**, rồi nhấn **Giờ nhanh** (giờ bắt đầu sau khoảng 30 giây), **Tạo phiên**. **Kết quả mong đợi:** Phiên xuất hiện trong danh sách, chuyển OPEN → RUNNING đúng giờ. **Nếu lỗi:** Kiểm tra item đã được chọn, giờ kết thúc lớn hơn giờ bắt đầu và giá hợp lệ; item đã gắn phiên sẽ không sửa/xóa được.

**Bước 3 – Mục tiêu:** Thấy cập nhật từ hai Bidder. **Thao tác:** Đăng nhập hai Bidder ở hai cửa sổ, mở cùng phiên bằng **Xem chi tiết**. Bidder A nhập giá lớn hơn giá hiện tại rồi nhấn **Đặt giá**; sau đó B đặt cao hơn. **Kết quả mong đợi:** Cả hai màn chi tiết thấy giá/người dẫn đầu đổi, lịch sử và biểu đồ thêm điểm. **Nếu lỗi:** Kiểm tra cả hai đang mở đúng ID phiên RUNNING; thử giá bằng/thấp hơn để thấy `BID_TOO_LOW` thay vì một bid được lưu.

**Bước 4 – Mục tiêu:** Kiểm tra kết thúc và lỗi thường gặp. **Thao tác:** Đợi hết giờ, nhấn **Xem màn kết quả**. Seller hoặc Admin thử **Đánh dấu đã thanh toán** với phiên có người thắng, hoặc **Hủy phiên** phù hợp. Thử nhập giá chữ/âm, tạo giờ sai, tắt server khi client đang mở, rồi đăng xuất/đăng nhập lại. **Kết quả mong đợi:** Hai cửa sổ thấy FINISHED và winner nếu có bid; thao tác sai hiện thông báo, client báo mất kết nối. **Nếu lỗi:** Kiểm tra quyền Seller/Admin, trạng thái FINISHED trước khi PAID/CANCELED; khởi động lại server rồi đăng nhập lại vì token trong bộ nhớ server đã mất.

JUnit `FxmlContractTest` kiểm tra resource, controller, fx:id, onAction và tải toàn bộ bảy FXML qua JavaFX toolkit trên Windows có desktop. `GuiFlowIT` kiểm tra click luồng chính với server thật; network integration tests kiểm tra thêm quyền, lỗi và bid đồng thời. Bố cục/thẩm mỹ và các nhánh Seller/Admin/lỗi vẫn cần quan sát thủ công theo checklist trên máy demo.

## Auto-bid, gia hạn và biểu đồ

Trong màn chi tiết của Bidder, nhập **Giá tối đa** và **Bước giá** rồi lưu auto-bid; cấu hình riêng được lấy từ server bằng `GET_AUTO_BID`, có thể xóa bằng `REMOVE_AUTO_BID`. LineChart bên dưới lịch sử bid dùng giây từ đầu phiên trên trục X và VND trên trục Y. `BID_UPDATED` tải lại lịch sử và cập nhật biểu đồ; `AUCTION_EXTENDED` cập nhật giờ kết thúc/countdown trực tiếp. Server mặc định gia hạn 30 giây nếu bid hợp lệ trong 30 giây cuối; có thể đổi bằng tham số của `run-server.ps1`.

`GuiFlowIT` ở lần nghiệm thu cuối đã chạy với server thật: thao tác lưu/tắt auto-bid trên GUI và xác nhận chart của Bidder còn lại tăng điểm sau event. Max cap và end time gia hạn được kiểm tra bằng service/socket tests; vẫn cần quan sát trực quan countdown và bố cục trên máy demo. Test FXML tải thành công màn chi tiết có chart.

Để kiểm tra nhánh Admin, chạy `scripts/run-demo.ps1 -DatabasePath data/gui-admin-check.db` trong một PowerShell với DB thử mới, rồi `scripts/test-gui.ps1 -Admin` ở PowerShell khác. `GuiAdminIT` dùng `demo_admin`, kiểm tra sai mật khẩu, danh sách 3 sản phẩm/3 phiên và logout. Dừng server trước khi xóa DB thử do chính lần kiểm tra tạo.

**Ghi nhớ:** `GuiFlowIT` đã kiểm tra một luồng chính tự động, còn bố cục và vài nút Seller/Admin cần quan sát bằng mắt trên máy demo. Lệnh test trong [TEST_CASES.md](TEST_CASES.md); mã màn hình ở `client/src/main/resources/view` và `client/src/main/java/com/btl/auction/client/controller`.
