# Câu hỏi bảo vệ và trả lời ngắn

**Mục đích:** Ôn lại lý do thiết kế sau khi đã đọc [ARCHITECTURE.md](ARCHITECTURE.md) và [CODE_WALKTHROUGH.md](CODE_WALKTHROUGH.md). Mỗi câu dưới đây gợi một câu trả lời ngắn; khi bảo vệ, hãy mở file/test tương ứng và diễn giải bằng ví dụ đặt giá.

1. **Đóng gói ở đâu?** Field trong `Entity`, `User`, `Item`, `Auction` là private; constructor kiểm tra dữ liệu, getter chỉ đọc.
2. **Kế thừa ở đâu?** `Bidder`, `Seller`, `Admin` kế thừa `User`; ba loại sản phẩm kế thừa `Item`.
3. **Đa hình ở đâu?** `getRole`, `getCategory`, `getDetail` gọi qua kiểu cha và trả hành vi của lớp con.
4. **Trừu tượng ở đâu?** `Entity`, `User`, `Item` là abstract; caller làm việc với hợp đồng chung.
5. **Tại sao dùng DTO?** DTO truyền snapshot qua JSON, không phơi DAO hay password hash.
6. **Singleton nào?** `AuctionManager.getInstance()` quản lý một tập khóa theo auction trong tiến trình.
7. **Singleton có giữ giá thắng không?** Không; SQLite và `AuctionService` giữ trạng thái chính.
8. **Factory Method nào?** `ItemFactory` khai báo `create`; ba concrete factory ghi đè để tạo `Electronics`, `Art`, `Vehicle`. `ItemCategory.factory()` chọn creator.
9. **Factory giúp gì?** `ProductService` dùng creator qua kiểu `ItemFactory`, không tự rẽ nhánh constructor; thêm loại mới không sửa creator gốc.
10. **Observer hoạt động thế nào?** Service enqueue event sau commit; publisher chuyển cho broadcaster; subscriber socket nhận.
11. **Tại sao không phát bid lỗi?** Transaction thất bại không đổi trạng thái, nên không có event để phát.
12. **MVC phân chia ra sao?** FXML là view, controller xử lý thao tác, SocketClient/model DTO nối dữ liệu; nghiệp vụ ở server service.
13. **Vì sao dùng TCP Socket?** Đáp ứng đề, giữ kết nối để server đẩy event không cần polling.
14. **Framing JSON thế nào?** Một object UTF-8 mỗi dòng LF, tối đa 1 MiB.
15. **Ghép response với request nào?** `requestId` trong request/response; event không có requestId.
16. **Response và event có thể xen kẽ không?** Có; client phân loại frame rồi hoàn tất đúng future hoặc gọi event listener.
17. **UI có bị block bởi socket không?** Đọc mạng ở thread nền; controller cập nhật UI qua `Platform.runLater`.
18. **Sửa UI từ callback socket trực tiếp được không?** Không, JavaFX chỉ cho sửa trên Application Thread.
19. **Ngăn lost update thế nào?** Khóa theo auction trong `AuctionManager`, transaction SQLite và cập nhật giá có điều kiện theo giá cũ.
20. **Tại sao có khóa lẫn transaction?** Khóa xếp thứ tự thao tác cùng phiên trong tiến trình; transaction bảo đảm ghi giá, leader và bid cùng thành công hoặc rollback.
21. **Dùng ReentrantLock hay synchronized?** `AuctionManager` dùng khóa theo ID; `ClientSession` dùng writer queue để tránh JSON trộn. Không dùng một khóa toàn server cho mọi bid.
22. **Hai client cùng bid bằng giá thì sao?** Service đọc giá mới trong khóa; bid đến sau với giá không cao hơn bị `BID_TOO_LOW`.
23. **SQLite DAO làm gì?** DAO chỉ SQL và ánh xạ hàng; service kiểm tra quyền, giá, thời gian và transaction.
24. **Tại sao chỉ server mở SQLite?** Client không thể tin cậy và cần một nơi quyết định giá/quyền nhất quán.
25. **Token có vai trò gì?** Server tạo token ngẫu nhiên sau LOGIN; dispatcher lấy actor từ token thay vì userId client gửi.
26. **Scheduler tránh đóng theo end time cũ thế nào?** `closeExpired` đọc trạng thái/end time hiện tại dưới cùng khóa và transaction; anti-sniping ghi end time mới trước event.
27. **Auto-bid có thể lặp vô hạn không?** Không; calculator chọn tối đa một bid proxy trong một lần xử lý và không vượt max.
28. **JUnit test gồm những lớp nào?** Unit cho model/protocol/thuật toán, integration cho SQLite/service/socket, GUI smoke riêng với desktop.
29. **JaCoCo và Checkstyle khác nhau thế nào?** JaCoCo đo dòng code chạy qua test; Checkstyle kiểm tra quy tắc mã nguồn; cả hai chạy ở verify.
30. **GitHub Actions làm gì?** Checkout, cài Temurin JDK17, `mvn -B clean verify`, lưu báo cáo; chỉ xác nhận xanh khi workflow chạy trên GitHub.
31. **Serialization/File I/O nằm ở đâu?** `SnapshotService` export/import JSON bằng Gson, còn SQLite là kho chính.
32. **Restart mất gì và giữ gì?** SQLite giữ sản phẩm, phiên, bid, winner; token trong bộ nhớ mất và người dùng đăng nhập lại.
