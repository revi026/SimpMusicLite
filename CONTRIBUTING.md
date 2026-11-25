# Đóng góp cho SimpMusic Lite

SimpMusic Lite là bản fork cá nhân của [SimpMusic](https://github.com/maxrave-dev/SimpMusic). Mục tiêu duy nhất của nó là **chạy nhẹ trên điện thoại Android cấu hình thấp**.

Tính năng hay lỗi không liên quan đến bản Lite thì nên đóng góp thẳng cho dự án gốc, theo quy định của dự án đó.

## Trước khi gửi pull request

- **Thay đổi lớn:** hãy [tạo issue](https://github.com/revi026/SimpMusicLite/issues/new/choose) để trao đổi trước. Sửa lỗi nhỏ thì gửi thẳng cũng được.
- **Nhánh:** tách nhánh từ `lite` và gửi pull request vào `lite`.
- **Tự kiểm tra:** pull request không được tự động build. Hãy ghi rõ bạn đã thử thế nào, trên máy nào.

## Quy ước

- Viết theo phong cách của mã xung quanh. Mã, chú thích và nội dung commit viết bằng tiếng Anh.
- Đánh dấu mỗi chỗ cố ý làm khác bản gốc bằng chú thích `// Lite fork:`.
- Ưu tiên tắt, chặn hoặc thay thế hơn là xóa mã dùng chung với bản gốc, để còn cập nhật theo bản gốc được.
- `core` là submodule của dự án gốc: thay đổi trong đó phải ở dạng patch trong [`patches/core`](patches/core), đừng đổi con trỏ submodule.
- Chuỗi hiển thị mới cần có bản tiếng Anh và tiếng Việt.

## Hiệu năng

Máy mục tiêu có GPU yếu, nên khi không có gì thay đổi trên màn hình thì app không nên vẽ gì cả.

- Không thêm hoạt ảnh chạy không ngừng.
- Không đưa blur, liquid glass hay lớp vẽ đệm cập nhật mỗi khung hình trở lại.
- Không đọc cả đối tượng timeline khi dựng giao diện: nó đổi mỗi 100 ms. Chỉ đọc giá trị thật sự hiển thị.
- Tránh chồng nhiều lớp nền phủ toàn màn hình.

## An toàn

Đừng đưa cookie, API key, token, file khóa ký hay thông tin cá nhân vào mã, commit, ảnh chụp hoặc log.

## Giấy phép

Đóng góp được phát hành theo [GPL-3.0](LICENSE), cùng giấy phép với dự án.
