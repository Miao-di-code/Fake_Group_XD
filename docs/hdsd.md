# Hướng dẫn sử dụng Online Auction System

## Khởi động nhanh — sao chép và dán lệnh

Giải nén thư mục `BTL_LTNC` trên **Windows** vào đường dẫn không có dấu cách, ví dụ `D:\BTL_LTNC` hoặc `C:\BTL_LTNC`. Mở thư mục đó, nhấp chuột phải vào chỗ trống và chọn **Open in Terminal**; chọn tab **PowerShell**. Dán toàn bộ khối lệnh sau vào **cửa sổ thứ nhất** rồi nhấn Enter:

```powershell
if (-not (Test-Path '.\_env\Library\bin\mvn.cmd')) { conda env create -p '.\_env' -f '.\environment.yml' }
.\scripts\Use-LocalConda.ps1
mvn -B install -DskipTests
if ($LASTEXITCODE -ne 0) { throw 'Không thể chuẩn bị ứng dụng. Hãy kiểm tra kết nối Internet.' }
$env:AUCTION_DEMO_PASSWORD = 'DemoPass123!'
.\scripts\run-demo.ps1 -DatabasePath 'data/user-guide-demo.db'
```

Đợi đến khi thấy dòng **`Auction server listening on 127.0.0.1:5555`**. Giữ nguyên cửa sổ thứ nhất. Mở **một cửa sổ PowerShell mới trong cùng thư mục `BTL_LTNC`** và dán:

```powershell
.\scripts\run-client.ps1
```

Một cửa sổ đăng nhập sẽ hiện ra. Muốn hai người đặt giá cùng lúc trên máy này, mở thêm một hoặc hai cửa sổ PowerShell trong cùng thư mục và chạy lại lệnh `run-client.ps1` ở mỗi cửa sổ. **Mở lần lượt:** đợi cửa sổ đăng nhập trước xuất hiện rồi mới chạy lệnh ở cửa sổ tiếp theo. Khi dùng xong, đóng các cửa sổ đấu giá rồi nhấn **Ctrl+C** trong cửa sổ thứ nhất.

**Điều kiện để lệnh trên chạy:** Máy dùng Windows và có giao diện màn hình. Nếu thư mục tải về chưa có `_env`, máy cần cài Anaconda hoặc Miniconda và có Internet ở lần đầu. Nếu PowerShell báo không tìm thấy `conda`, hãy cài Anaconda/Miniconda, mở lại PowerShell rồi dán lại khối lệnh đầu. Các lần mở sau vẫn dùng đúng hai khối lệnh trên. Hiện ứng dụng mở bằng cửa sổ trên **cùng máy**, chưa mở bằng trình duyệt web và chưa cho máy khác kết nối theo cấu hình này.

## Cài môi trường bằng `environment.yml` với Miniconda

Chỉ cần làm phần này **một lần** sau khi tải dự án về máy. Máy được giả định đã cài Miniconda.

1. Mở thư mục `BTL_LTNC`.
2. Nhấp chuột phải vào chỗ trống trong thư mục, chọn **Open in Terminal** và bảo đảm terminal đang dùng PowerShell.
3. Kiểm tra Miniconda đã hoạt động:

```powershell
conda --version
```

Nếu lệnh hiện phiên bản Conda, tiếp tục chạy:

```powershell
conda env create -p .\_env -f .\environment.yml
```

Miniconda sẽ đọc `environment.yml`, tải Java 17 và Maven rồi đặt chúng trong thư mục `_env` của dự án. Lần đầu cần có Internet và có thể mất vài phút.

Sau khi hoàn tất, chạy các lệnh sau để kiểm tra:

```powershell
.\scripts\Use-LocalConda.ps1
java -version
mvn -version
```

Kết quả đúng là `java -version` hiển thị Java 17 và `mvn -version` hiển thị Maven 3.9.x. Sau đó khởi động hệ thống bằng:

```powershell
$env:AUCTION_DEMO_PASSWORD = 'DemoPass123!'
.\scripts\run-demo.ps1 -DatabasePath 'data/user-guide-demo.db'
```

Giữ cửa sổ này chạy. Mở một PowerShell mới trong cùng thư mục và mở giao diện bằng:

```powershell
.\scripts\run-client.ps1
```

Từ lần sử dụng tiếp theo, không cần chạy lại `conda env create`. Chỉ cần chạy `run-demo.ps1`, sau đó chạy `run-client.ps1` trong cửa sổ khác.

Nếu PowerShell báo không nhận lệnh `conda`, mở **Miniconda Prompt**, chạy:

```bat
conda init powershell
```

Đóng toàn bộ PowerShell, mở lại rồi thử `conda --version`. Nếu báo thư mục `_env` đã tồn tại thì môi trường thường đã được tạo; hãy chạy ba lệnh kiểm tra Java/Maven ở trên trước khi cài lại.

## Tài khoản mẫu

Sau khi cửa sổ thứ nhất khởi động thành công, dùng các tài khoản sau. Tên đăng nhập và mật khẩu cần nhập đúng chữ hoa, chữ thường và dấu `!`.

| Vai trò | Tên đăng nhập | Mật khẩu |
| --- | --- | --- |
| Người quản trị (Admin) | `demo_admin` | `DemoPass123!` |
| Người bán (Seller) | `demo_seller` | `DemoPass123!` |
| Người đấu giá A (Bidder) | `demo_bidder_a` | `DemoPass123!` |
| Người đấu giá B (Bidder) | `demo_bidder_b` | `DemoPass123!` |

Đây là tài khoản để thử ứng dụng. Ngoài ra, ở màn đăng nhập có nút **Tạo tài khoản** để đăng ký tài khoản Người bán hoặc Người đấu giá mới. Không thể tự đăng ký tài khoản Người quản trị.

## Mọi người đều có thể xem gì?

Sau khi đăng nhập, màn **Phiên đấu giá** cho xem danh sách các phiên. Chọn một phiên rồi nhấn **Xem chi tiết** để xem sản phẩm, giá khởi điểm, giá hiện tại, người dẫn đầu, thời gian còn lại, lịch sử đặt giá và biểu đồ giá. Nhấn **← Danh sách** để quay lại; nhấn **Tải lại danh sách** nếu muốn xem danh sách mới nhất. Nhấn **Đăng xuất** để đổi tài khoản.

Các trạng thái dễ hiểu:

| Trạng thái | Ý nghĩa |
| --- | --- |
| `OPEN` | Phiên chưa đến giờ bắt đầu. |
| `RUNNING` | Phiên đang nhận giá. |
| `FINISHED` | Hết giờ, đã xác định người thắng nếu có người đặt giá. |
| `PAID` | Người có quyền đã đánh dấu phiên là đã thanh toán. |
| `CANCELED` | Người có quyền đã đánh dấu phiên là đã hủy. |

`PAID` chỉ là trạng thái hiển thị trong ứng dụng; hệ thống chưa thu tiền hoặc chuyển tiền thật.

## Người đấu giá: đặt giá và theo dõi kết quả

1. Đăng nhập bằng `demo_bidder_a` hoặc `demo_bidder_b`. Chọn một phiên `RUNNING` rồi nhấn **Xem chi tiết**.
2. Xem **Giá hiện tại**, nhập một số tiền **cao hơn** vào ô **Giá bid (VND)** rồi nhấn **Đặt giá**. Nhập số VND liền nhau, ví dụ `1100000`.
3. Nếu giá hợp lệ, màn hình hiện **Đặt giá thành công**. Cả hai cửa sổ đang xem cùng phiên sẽ thấy giá/người dẫn đầu thay đổi; lịch sử và biểu đồ cũng được cập nhật.
4. Nếu giá bằng hoặc thấp hơn giá hiện tại, hoặc phiên chưa bắt đầu/đã hết giờ, ứng dụng sẽ báo lỗi. Xem lại trạng thái và giá rồi thử một mức phù hợp.
5. Muốn ứng dụng tự trả giá trong giới hạn của mình, nhập **Giá tối đa (VND)** và **Bước giá (VND)**, rồi nhấn **Lưu auto-bid**. Giá tối đa phải lớn hơn giá hiện tại; bước giá phải là số dương. Nhấn **Tắt auto-bid** nếu không muốn dùng nữa. Cấu hình giá tối đa của bạn không hiển thị cho người đấu giá khác.
6. Khi phiên kết thúc, nhấn **Xem màn kết quả** để xem giá cuối và người thắng. Nếu không ai đặt giá, phiên không có người thắng.

Nếu có giá hợp lệ trong **30 giây cuối**, thời gian kết thúc mặc định được kéo dài thêm **30 giây**. Thời gian còn lại trên màn chi tiết sẽ thay đổi theo giờ mới.

**Cách thử hai người:** Mở hai cửa sổ đấu giá. Cửa sổ A đăng nhập `demo_bidder_a`, cửa sổ B đăng nhập `demo_bidder_b`; cả hai mở cùng phiên `RUNNING`. A đặt giá trước, sau đó B nhập giá cao hơn. Quan sát giá đổi ở cả hai cửa sổ mà không cần bấm tải lại liên tục.

## Người bán: đăng sản phẩm và mở phiên

1. Đăng nhập `demo_seller`. Ở màn danh sách, nhấn **Quản lý sản phẩm**.
2. Chọn **Loại** sản phẩm, nhập **Tên**, **Mô tả**, **Giá khởi điểm (VND)** và **Chi tiết loại**. Nhấn **Thêm sản phẩm**. Sản phẩm mới xuất hiện trong **Sản phẩm của tôi**.
3. Muốn sửa, chọn sản phẩm trong danh sách, đổi thông tin rồi nhấn **Sửa sản phẩm đã chọn**. Muốn xóa, chọn sản phẩm rồi nhấn **Xóa sản phẩm đã chọn**. Sản phẩm đã có phiên đấu giá không thể sửa hoặc xóa.
4. Muốn mở phiên, chọn một sản phẩm **chưa có phiên**. Nhấn **Giờ nhanh** để điền giờ bắt đầu sau khoảng 30 giây và giờ kết thúc sau khoảng 2 phút 30 giây; hoặc tự nhập theo mẫu `dd/MM/yyyy HH:mm:ss`. Nhấn **Tạo phiên**. Giờ bắt đầu cần ở tương lai và giờ kết thúc phải muộn hơn.
5. Xem **Các phiên của tôi** hoặc quay lại **← Danh sách**. Đến giờ bắt đầu, phiên chuyển sang `RUNNING` để Bidder đặt giá.
6. Sau khi phiên `FINISHED`, người bán có thể mở chi tiết và nhấn **Đánh dấu đã thanh toán** nếu có người thắng, hoặc **Hủy phiên** khi phù hợp.

Ba sản phẩm mẫu có sẵn là **Demo Laptop**, **Demo Painting** và **Demo Bicycle**. Phiên Demo Laptop bắt đầu ngay khi tạo dữ liệu mẫu và hết hạn sau khoảng 30 phút; các phiên còn lại bắt đầu muộn hơn. Nếu phiên mẫu đã hết giờ, hãy tạo sản phẩm và phiên mới theo bước 2–4.

## Người quản trị: xem và quản lý phiên

1. Đăng nhập `demo_admin`, nhấn **Quản trị** ở màn danh sách.
2. Xem **Tất cả phiên đấu giá** và **Sản phẩm và chủ sở hữu**. Chọn một phiên, nhấn **Xem chi tiết** nếu cần xem giá, lịch sử và kết quả.
3. Với phiên đã `FINISHED`, chọn phiên rồi nhấn **Đánh dấu đã thanh toán** nếu có người thắng, hoặc **Hủy phiên đã kết thúc** khi phù hợp. Nhấn **Tải lại** để xem trạng thái mới.
4. Nhấn **← Danh sách** để quay về màn chính; nhấn **Đăng xuất** khi dùng xong.

Người quản trị không dùng màn **Tạo tài khoản** để tạo thêm Admin. Những nút quản lý chỉ hoạt động khi phiên ở trạng thái cho phép.

## Khi gặp trục trặc

| Điều thấy trên màn hình | Cách xử lý |
| --- | --- |
| Không xuất hiện cửa sổ đăng nhập | Kiểm tra cửa sổ thứ nhất đã hiện `Auction server listening...`; chạy lại lệnh mở cửa sổ đấu giá trong đúng thư mục `BTL_LTNC`. |
| Báo sai tên đăng nhập/mật khẩu | Dùng đúng bảng tài khoản trên. Nếu bạn đã có dữ liệu mẫu cũ với mật khẩu khác, dùng mật khẩu đã đặt trước đó. |
| Cửa sổ thứ nhất báo tài khoản mẫu cũ dùng mật khẩu khác | Trong lệnh `run-demo.ps1`, đổi `data/user-guide-demo.db` thành tên mới như `data/user-guide-demo-2.db`, rồi chạy lại. Dữ liệu cũ vẫn được giữ. |
| Không thấy phiên đang nhận giá | Nhấn **Tải lại danh sách**; xem trạng thái `RUNNING`. Phiên mẫu có thể đã hết giờ; Seller tạo phiên mới. |
| Đặt giá không thành công | Nhập số VND cao hơn giá hiện tại và kiểm tra phiên còn `RUNNING`. |
| Cửa sổ báo mất kết nối | Kiểm tra cửa sổ thứ nhất còn chạy. Nếu đã đóng, khởi động lại rồi đăng nhập lại. |
| Báo cổng 5555 đang được dùng | Có thể hệ thống đã chạy ở một cửa sổ khác. Đóng cửa sổ chạy cũ bằng **Ctrl+C** rồi thử lại. |

**Ghi nhớ:** Giữ cửa sổ thứ nhất đang chạy trong suốt thời gian sử dụng. Mỗi cửa sổ đấu giá có thể đăng nhập một tài khoản khác nhau để thử nhiều người cùng tham gia.
