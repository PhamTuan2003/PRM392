# CalculatorPRM - Ứng dụng chat bí mật dưới vỏ bọc máy tính

## Giới thiệu

**CalculatorPRM** là một ứng dụng chat bảo mật dành cho Android, được “ngụy trang” dưới dạng một ứng dụng máy tính thông thường. Bạn có thể sử dụng nó như một máy tính bình thường, nhưng khi nhập đúng mã PIN, bạn sẽ truy cập vào thế giới chat bí mật với bạn bè, gửi tin nhắn, hình ảnh, và nhiều tính năng thú vị khác.

---

## Tính năng nổi bật

### 1. Ngụy trang hoàn hảo
- Ứng dụng hiển thị ngoài màn hình chính như một app máy tính thông thường.
- Giao diện máy tính hiện đại, có thể thực hiện các phép tính cơ bản.
- Nhập đúng mã PIN (mặc định: 1234) để mở khóa và truy cập vào tính năng chat.

### 2. Bảo mật với mã PIN
- Đổi mã PIN dễ dàng trong phần cài đặt.
- Không ai biết bạn đang dùng app chat nếu không có mã PIN.

### 3. Đăng ký & đăng nhập
- Đăng ký tài khoản mới bằng email và mật khẩu.
- Đăng nhập an toàn với Firebase Authentication.

### 4. Quản lý bạn bè
- Gửi, nhận, chấp nhận hoặc từ chối lời mời kết bạn.
- Danh sách bạn bè hiển thị rõ ràng, dễ tìm kiếm.

### 5. Chat real-time
- Nhắn tin với bạn bè theo thời gian thực.
- Gửi và nhận hình ảnh trong cuộc trò chuyện.
- Xem ảnh ở chế độ toàn màn hình, hỗ trợ zoom.

### 6. Chỉnh sửa hồ sơ cá nhân
- Đổi tên, trạng thái, mật khẩu, avatar ngay trong app.
- Thay đổi được cập nhật tức thì lên Firebase.

### 7. Lên lịch gửi tin nhắn
- Đặt lịch gửi tin nhắn tự động vào thời điểm mong muốn.
- Quản lý, hủy, theo dõi trạng thái các tin nhắn đã lên lịch.

### 8. Giao diện đẹp, dễ dùng
- Thiết kế theo Material Design, hỗ trợ cả Light/Dark mode.
- Tối ưu cho trải nghiệm người dùng hiện đại.

---

## Cách sử dụng

1. Cài đặt app và mở lên, bạn sẽ thấy giao diện máy tính.
2. Nhập mã PIN bí mật (mặc định: 1234) rồi nhấn "=" để mở khóa tính năng chat.
3. Đăng ký hoặc đăng nhập tài khoản để bắt đầu kết bạn, nhắn tin.
4. Vào phần cài đặt để đổi mã PIN, chỉnh sửa hồ sơ, hoặc xem các tin nhắn đã lên lịch.
5. Sử dụng như một app chat thông thường, nhưng hoàn toàn bí mật!

---

## Công nghệ sử dụng

- Android SDK, Java
- Firebase Authentication & Realtime Database
- Firebase Storage (lưu ảnh)
- Material Design
- Picasso (load ảnh)
- AlarmManager (lên lịch gửi tin nhắn)

---

## Một số lưu ý

- Đừng quên đổi mã PIN mặc định để tăng bảo mật!
- Ứng dụng yêu cầu quyền truy cập camera, bộ nhớ để gửi/nhận ảnh.
- Cần kết nối internet để sử dụng các tính năng chat, gửi ảnh, đồng bộ dữ liệu.

---

## Hỗ trợ

Nếu bạn gặp vấn đề khi sử dụng app, hãy kiểm tra:
- Đã cấp đủ quyền cho app chưa (camera, bộ nhớ)?
- Đã thêm file `google-services.json` vào thư mục `app/` chưa?
- Kết nối internet có ổn định không?

---

**CalculatorPRM** – Chat bí mật, an toàn, tiện lợi, và không ai biết ngoài bạn! 