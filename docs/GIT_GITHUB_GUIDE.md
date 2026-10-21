# Làm việc nhóm với Git và GitHub

**Mục đích:** Hiểu những thao tác tối thiểu để bốn thành viên chia việc và kiểm tra thay đổi. Đọc [phân công](MODULE_OWNERSHIP.md) trước. Các lệnh Git chỉ dùng khi đã clone một repository có thư mục `.git`; workspace tài liệu hiện tại chưa có `.git`, nên không thể coi kế hoạch commit là lịch sử thật.

## Từ vựng qua một ví dụ

**Git** lưu các phiên bản code trên máy; **GitHub** là dịch vụ lưu và chia sẻ **repository** (kho dự án) qua mạng. **Clone** tải kho về máy. **Branch** là nhánh để phát triển một việc riêng; ví dụ `feature/bid-tests`. **Commit** ghi một mốc thay đổi vào lịch sử local. **Push** gửi commit lên GitHub; **pull** lấy thay đổi mới về. **Pull Request (PR)** là đề nghị đưa branch vào nhánh chung để người khác xem; **merge** là thao tác hợp nhất. **Conflict** xảy ra khi hai người sửa cùng vùng và Git không tự chọn được. **Issue** là phiếu ghi việc/lỗi. **GitHub Actions** chạy workflow, ở dự án này là `.github/workflows/ci.yml`, sau push/PR. Workflow cần kết quả trên GitHub mới biết chạy xanh.

**CI** là kiểm tra tích hợp liên tục: mỗi lần push/PR, workflow tự build và test để phát hiện lỗi sớm. **CD** là tự động phát hành/triển khai sau khi kiểm tra; dự án hiện chưa cấu hình CD. `ci.yml` chỉ checkout code, cài JDK 17, chạy `mvn -B clean verify` rồi tải báo cáo test/coverage lên artifact.

Ví dụ: người phụ trách networking sửa `SocketClient.java` trên branch riêng, commit và push; người phụ trách GUI mở PR, kiểm tra callback có còn đúng, rồi nhóm merge khi test đạt. Giao thức chung phải đối chiếu [CONTRACTS.md](CONTRACTS.md) và [API_PROTOCOL.md](API_PROTOCOL.md).

## Một vòng làm việc trong VS Code và PowerShell

**Mục tiêu:** Tạo thay đổi nhỏ, có kiểm tra và nhờ đồng đội xem. **Thao tác:** Nếu chưa có bản clone, mở repository của nhóm trên GitHub, chọn **Code → HTTPS → Copy**. Trong VS Code nhấn `Ctrl+Shift+P`, chọn **Git: Clone**, dán URL vừa sao chép, chọn thư mục lưu và mở thư mục vừa clone. Mở Terminal ở đó, kiểm tra `git status`; chỉ tiếp tục nếu không có lỗi. Sau đó chạy:

```powershell
git status
git pull
git switch -c feature/bid-tests
```

Sửa file được phân công. Chạy `./scripts/run-tests.ps1` nếu làm trên Windows với `_env`, hoặc `mvn -B clean verify` ở máy có JDK/Maven và mạng. Xem khác biệt bằng `git diff`; chỉ chọn file liên quan:

```powershell
git add server/src/test/java/com/btl/auction/server/service/BackendIntegrationTest.java
git commit -m "test(core): cover concurrent bids"
git push -u origin feature/bid-tests
```

Mở repository trên GitHub, tạo Pull Request từ branch này, mô tả thay đổi và test đã chạy, nhờ người khác review. **Kết quả mong đợi:** PR có diff, review và kết quả Actions. **Nếu lỗi:** `fatal: not a git repository` nghĩa là thư mục hiện tại chưa phải bản clone. `git pull` có conflict thì xem từng file có dấu `<<<<<<<`, chọn nội dung đúng sau khi trao đổi với người cùng sửa, chạy test lại rồi mới commit. Tránh `git add .` khi có database, token hoặc file thử chưa kiểm tra.

## Phối hợp bốn vai trò

| Người phụ trách | Branch/PR thường chạm | Người nên review |
| --- | --- | --- |
| 1: Auction Core | `AuctionService`, auction tests, auto-bid | 4 xem transaction/DAO; 3 xem event |
| 2: JavaFX GUI | FXML, controller, GUI tests | 3 xem request/response; 1 xem quy tắc bid hiển thị |
| 3: Networking | protocol, socket, broadcast, network tests | 2 xem callback UI; 1 xem event |
| 4: User/Product/Database/CI | auth, item, schema, workflow | 1 xem transaction; 3 xem DTO/protocol |

Mỗi người cần chạy thử luồng GUI → TCP → service → SQLite → event, vì phần mình sửa có thể ảnh hưởng phần khác. [Kế hoạch 6 tuần](6_WEEK_PLAN.md) là gợi ý chia việc và commit, chưa chứng minh ai đã thực hiện. [Báo cáo nghiệm thu](FINAL_ACCEPTANCE_REPORT.md) ghi rõ phần đóng góp chưa xác minh.

**Ghi nhớ:** Git giữ lịch sử; GitHub giúp phối hợp và chạy CI. Chỉ báo đã kiểm thử khi có kết quả thực từ máy hoặc workflow.
