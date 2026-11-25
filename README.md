<div align="center">

<img src="composeApp/src/commonMain/composeResources/drawable/app_icon.png" width="120" alt="Logo SimpMusic Lite">

# SimpMusic Lite

Bản fork cá nhân của [SimpMusic](https://github.com/maxrave-dev/SimpMusic), được tinh gọn để chạy nhẹ hơn trên điện thoại Android cấu hình thấp.

**Phiên bản 1.1.0** · dựa trên SimpMusic 2.6.0 · GPL-3.0

*A personal, lighter fork of SimpMusic for low-end Android phones. Not an official SimpMusic release.*

</div>

> [!NOTE]
> Đây **không phải** bản phát hành chính thức của SimpMusic, và tác giả gốc không hỗ trợ bản này. Bản Lite được chỉnh và đo hiệu năng trên một máy: Galaxy J7 Prime chạy ROM port Android 15. Bản 32-bit đã được thử cơ bản (mở app, tìm kiếm, phát nhạc, lời bài hát) trên một máy SM-J330G chạy ROM cook, Android 10.

## Khác gì so với bản gốc

**Gỡ bỏ hoặc tắt**
- Toàn bộ hiệu ứng mờ (blur) và liquid glass
- Các kiểu màn hình phát khác, chỉ giữ kiểu Classic
- Thống kê nghe nhạc, Wrapped và Nghe cùng nhau
- Hộp thoại nhắc đánh giá, ủng hộ, đăng nhập và chia sẻ lời bài hát; banner quảng bá
- Kiểm tra cập nhật và thông báo blog
- Bước kiểm tra chữ ký "bản chính thức" (bản Lite ký bằng khóa riêng)

**Thay đổi và bổ sung**
- Thanh điều hướng đơn giản; Cài đặt chia thành trang chính và các trang con
- Nút **Dịch lời bài hát bằng AI** trong menu bài hát (dùng được với API tương thích OpenAI)
- Lời chạy theo từng từ trở thành tùy chọn, mặc định tắt
- Ảnh được tải đúng theo kích thước hiển thị
- Tên và logo riêng, cài song song được với SimpMusic gốc
- Có APK cho cả máy 64-bit và 32-bit

**Hiệu năng**
- Bỏ các hoạt ảnh chạy không ngừng; màn hình chỉ vẽ lại khi có thứ thay đổi
- Chữ chạy ở khoảng 12 khung hình/giây và dừng sau tối đa hai lượt
- Nền đầu trang chủ và màn hình phát được gộp thành một dải màu. Trên máy thử, thời gian mỗi khung hình khi vuốt giảm từ khoảng 95 ms xuống 22–29 ms ở đầu trang chủ, và từ khoảng 79 ms xuống khoảng 28 ms ở màn hình phát (đo ít lượt, chỉ nên xem là số tham khảo)
- Giảm việc thừa trên luồng phát nhạc ([`patches/core`](patches/core))

## Tải về

**[Tải bản mới nhất ở trang Releases](https://github.com/revi026/SimpMusicLite/releases/latest)**, lấy file `.apk` hợp với máy.

- Yêu cầu Android 8.0 trở lên. Chọn file theo máy:
  - `arm64-v8a`: máy 64-bit, tức hầu hết điện thoại hiện nay.
  - `armeabi-v7a`: máy hoặc ROM 32-bit.
- Tên gói là `com.revi026.simpmusiclite`, khác bản gốc, nên **cài song song được** với SimpMusic. Hai app không dùng chung dữ liệu.
- Muốn mang dữ liệu sang: trong app cũ chọn Cài đặt → Dữ liệu & lưu trữ → **Sao lưu**, rồi chọn **Khôi phục** trong bản Lite.
- Android coi bản build trước khi đổi tên gói (`com.maxrave.simpmusic`) là một app khác. Sau khi đã chuyển dữ liệu, bạn có thể gỡ nó.
- File `.sha256` đi kèm dùng để kiểm tra APK tải về có nguyên vẹn không.

Muốn thử bản mới nhất chưa phát hành: tải artifact `simpmusic-lite` trong tab [Actions](https://github.com/revi026/SimpMusicLite/actions) (cần đăng nhập GitHub, được lưu 14 ngày).

## Báo lỗi và góp ý

[Tạo issue mới](https://github.com/revi026/SimpMusicLite/issues/new/choose) rồi chọn mẫu **Báo lỗi**, **Lỗi phát nhạc** hoặc **Đề xuất tính năng**.

- Hãy ghi phiên bản app (Cài đặt → Giới thiệu & nhà phát triển), bản APK đã cài, đời máy và phiên bản Android.
- Issue là công khai: đừng dán cookie, API key, token hay thông tin tài khoản, và hãy đọc lại log trước khi gửi.
- Xin đừng gửi lỗi của bản Lite sang dự án gốc.

## Tự build

Fork repo này rồi chạy workflow [`lite-apk.yml`](.github/workflows/lite-apk.yml) (tự chạy khi push lên `lite`, hoặc chạy thủ công).

- Để các bản build sau cài đè được lên bản trước, thêm bốn secret: `SIGNING_KEY` (keystore mã hóa base64), `ALIAS`, `KEY_STORE_PASSWORD`, `KEY_PASSWORD`.
- Không có secret thì mỗi lần build dùng một khóa tạm khác nhau.
- Khi build trên máy, áp patch cho `core` trước: `git -C core apply "$PWD"/patches/core/*.patch`

## Người phát triển bản Lite

**[revi026](https://github.com/revi026)** · [Mã nguồn bản Lite](https://github.com/revi026/SimpMusicLite)

## Tác giả gốc

**SimpMusic** do **[maxrave-dev](https://github.com/maxrave-dev)** (Tuan Minh Nguyen Duc) tạo ra. Phần lớn mã nguồn và tính năng ở đây là công sức của tác giả và những người đóng góp cho dự án gốc.

- [Dự án gốc](https://github.com/maxrave-dev/SimpMusic) · [Trang web](https://simpmusic.org) · [Blog](https://www.simpmusic.org/blogs)
- [Ủng hộ tác giả gốc](https://github.com/sponsors/maxrave-dev)
- [Những người đóng góp cho dự án gốc](https://github.com/maxrave-dev/SimpMusic/graphs/contributors)

Dự án gốc ghi nhận: [InnerTune](https://github.com/z-huang/InnerTune/), [SmartTube](https://github.com/yuliskov/SmartTube), [SponsorBlock](https://sponsor.ajay.app/), ReturnYouTubeDislike, SimpMusic Lyrics, [LRCLIB](https://lrclib.net/) và [Metrolist](https://github.com/MetrolistGroup/Metrolist).

## Giấy phép

Phát hành theo [GNU GPL v3.0](LICENSE), cùng giấy phép với dự án gốc. Bản Lite là bản đã sửa đổi từ tháng 11/2026; chi tiết từng thay đổi nằm trong lịch sử commit của nhánh `lite`.

## Legal Disclaimer & Terms of Use

> Phần dưới đây giữ nguyên văn của dự án gốc, nên email trong đó là của tác giả gốc. Vấn đề riêng của bản Lite xin gửi qua [Issues](https://github.com/revi026/SimpMusicLite/issues) hoặc liên hệ GitHub [revi026](https://github.com/revi026).

### 1. 100% Free, Open-Source & Strictly Non-Commercial
SimpMusic is a fully open-source project (FOSS) created purely for educational purposes and personal use. **We do not sell this application, nor do we monetize it in any way.** There are no advertisements, no premium features, no subscriptions, and no hidden fees within the app. This project has absolutely no commercial value or financial intent.

### 2. A Custom Browser with Content Filtering
SimpMusic acts strictly as a specialized, third-party web browser and client. It simply parses the publicly available website content and APIs of YouTube and YouTube Music, rendering them in a custom user interface. The ad-free experience it provides is fundamentally no different from using a standard web browser (like Chrome, Firefox, or Brave) equipped with a common ad-blocking extension (such as uBlock Origin).

### 3. Support Content Creators
We deeply respect the hard work of artists, musicians, and content creators. **We strongly encourage all users to subscribe to [YouTube Premium](https://www.youtube.com/premium).** Purchasing a Premium subscription is the best way to financially support the creators you listen to and ensure the continued growth of the platform. SimpMusic is built as a proof-of-concept for developers and enthusiasts, not to harm creators' revenues.

### 4. No Hosting of Copyrighted Material
We do not host, upload, distribute, or store any audio, video, or copyrighted media files on our own servers. All content accessed through this application is stored entirely on Google's/YouTube's servers and remains the property of their respective copyright owners. The app merely acts as a conduit to stream publicly accessible links.

### 5. User Responsibility & Legal Contact
The software is provided "AS IS", without warranty of any kind. The developers of SimpMusic do not encourage or condone piracy. Users are solely responsible for ensuring their usage of this app complies with their local copyright laws and the Terms of Service of the platforms they access.

Because we do not host any media files, we cannot process DMCA takedown requests for audio or video content. However, if you represent a copyright holder or have legal concerns regarding the open-source code itself, please contact us via email at: **ndtminh2608@gmail.com**
