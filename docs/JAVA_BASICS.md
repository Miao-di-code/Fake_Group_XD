# Kiến thức tối thiểu để đọc dự án

**Mục đích:** Giúp người chưa học Java nhận ra các khái niệm sẽ gặp trong mã nguồn đấu giá. Chỉ cần biết mở file và đọc văn bản; chưa cần biết lập trình. Hãy thử tìm đúng file được nêu sau mỗi mục.

## Từ mã nguồn đến chương trình chạy

**Source code** là văn bản lập trình viên viết. Ví dụ `common/src/main/java/com/btl/auction/common/model/Auction.java` mô tả một phiên đấu giá. **Java** là ngôn ngữ dùng để viết phần lớn dự án này. **JDK** là bộ công cụ để biên dịch và chạy Java; dự án dùng JDK 17. **Biên dịch** biến `.java` thành `.class`. **JVM** là chương trình thực thi các `.class` đó. Khi sửa `.java`, cần build lại để chương trình chạy bản mới.

**Maven** đọc `pom.xml` để tải **dependency** (thư viện mà dự án cần), biên dịch và kiểm thử. Ví dụ `client/pom.xml` khai báo JavaFX; `server/pom.xml` khai báo SQLite JDBC. `mvn -B clean verify` chạy chuỗi build và kiểm tra. `target/` là kết quả Maven sinh ra; nó không phải nơi sửa code. Xem thao tác thật ở [hướng dẫn chạy](GETTING_STARTED.md).

## Class, object, field, method, package

**Class** là bản mô tả một kiểu dữ liệu; **object** là một đối tượng tạo từ class. `Auction` là class, còn phiên có ID 42 là một object `Auction`. **Field** là dữ liệu của object, chẳng hạn `currentPriceVnd`. **Method** là việc object có thể làm, chẳng hạn `acceptsBidsAt(now)`. Mở `Auction.java`: phần khai báo field ở đầu, constructor tạo object, getter đọc field, method kiểm tra thời gian ở cuối.

**Package** là nhóm class theo trách nhiệm. Dòng `package com.btl.auction.server.service;` cho biết `AuctionService` thuộc nhóm xử lý nghiệp vụ server. Tên package tương ứng với đường dẫn thư mục dưới `src/main/java`. **Module** là một phần Maven có `pom.xml` riêng: dự án có `common`, `server`, `client`. Module lớn hơn package.

## OOP trong ứng dụng

**OOP** (lập trình hướng đối tượng) tổ chức chương trình bằng class và object. **Đóng gói** giữ dữ liệu bên trong object và cho phép đọc/thay đổi theo quy tắc; ví dụ `Auction` kiểm tra trạng thái trong `transitionTo`. **Kế thừa** cho class con dùng phần chung của class cha: `Bidder`, `Seller`, `Admin` kế thừa `User`. **Đa hình** nghĩa là cùng lời gọi có thể cho kết quả tùy object cụ thể: `getRole()` trên từng loại `User` trả vai trò tương ứng. **Trừu tượng** giữ phần chung và chưa cho tạo object trực tiếp: `User` và `Item` là abstract.

**Interface** là hợp đồng các method mà class thực hiện. `AuctionListener` có method nhận sự kiện, để nơi phát sự kiện không cần biết ai đang nghe. **Abstract class** vừa có thể giữ dữ liệu/code chung vừa yêu cầu class con hoàn thiện hành vi; `Item` là ví dụ. Xem cách các class này phối hợp trong [kiến trúc](ARCHITECTURE.md).

## Thread, exception và dữ liệu trống

**Thread** là một luồng thực hiện công việc. `SocketServer` phục vụ nhiều kết nối bằng các thread; JavaFX có một Application Thread chuyên vẽ giao diện. Đọc socket trên thread giao diện sẽ làm cửa sổ đứng. **Concurrency** là nhiều công việc có thể chạy gần như cùng lúc. Hai người cùng đặt giá phải được `AuctionService` xử lý theo thứ tự nhất quán.

**Exception** là tín hiệu báo một thao tác thất bại. Ví dụ `ServiceException` mang mã `BID_TOO_LOW`; `RequestDispatcher` đổi nó thành response lỗi cho client. `null` là “chưa có giá trị”: `leaderUserId` có thể `null` nếu chưa ai đặt giá. Luôn xem code kiểm tra `null` trước khi dùng giá trị đó.

**Tự kiểm tra:** Mở `Auction.java` và chỉ ra class, field, method. Mở `ItemFactory.java` và ba concrete factory để tìm nơi tạo `Electronics`, `Art`, `Vehicle`. Sau đó đọc [tổng quan kiến trúc](ARCHITECTURE.md).
