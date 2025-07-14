# MessengerPRM - Android Messenger App

## 📱 Mô tả dự án
Ứng dụng messenger Android đơn giản sử dụng Firebase Authentication và Realtime Database để lưu trữ dữ liệu người dùng.

## 🚀 Tính năng
- ✅ Đăng ký tài khoản mới
- ✅ Đăng nhập với email/password
- ✅ Lưu trữ thông tin user trong Firebase Realtime Database
- ✅ Upload và lưu trữ ảnh profile dưới dạng Base64
- ✅ Hiển thị thông tin user và ảnh profile từ database
- ✅ **Chỉnh sửa profile** (tên, trạng thái, password, avatar)
- ✅ **Hệ thống kết bạn** (gửi/chấp nhận/từ chối lời mời)
- ✅ **Chat real-time** với bạn bè
- ✅ **Gửi hình ảnh** trong chat
- ✅ **Xem ảnh fullscreen** với zoom
- ✅ **Dark/Light theme**
- ✅ Giao diện đẹp với Material Design

## 🔧 Cấu hình Firebase

### 1. Tạo project Firebase
1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Tạo project mới
3. Thêm ứng dụng Android với package name: `com.example.messengerprm`

### 2. Cấu hình Authentication
1. Vào **Authentication** > **Sign-in method**
2. Bật **Email/Password**
3. Lưu cấu hình

### 3. Cấu hình Realtime Database
1. Vào **Realtime Database**
2. Tạo database mới
3. Chọn **Start in test mode** (cho development)
4. Copy URL database

### 4. Cấu hình Security Rules
```json
{
  "rules": {
    "user": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid",
        "userName": {
          ".validate": "newData.isString() && newData.val().length >= 2"
        },
        "status": {
          ".validate": "newData.isString() && newData.val().length <= 100"
        }
      }
    },
    "friendRequests": {
      "$toUid": {
        "$fromUid": {
          ".read": "$toUid === auth.uid || $fromUid === auth.uid",
          ".write": "$fromUid === auth.uid"
        }
      }
    },
    "friends": {
      "$uid": {
        "$friendUid": {
          ".read": "$uid === auth.uid || $friendUid === auth.uid",
          ".write": "$uid === auth.uid || $friendUid === auth.uid"
        }
      }
    },
    "chats": {
      "$chatRoomId": {
        ".read": "$chatRoomId.matches(/.*" + auth.uid + ".*/)",
        ".write": "$chatRoomId.matches(/.*" + auth.uid + ".*/)",
        "messages": {
          "$messageId": {
            ".read": "$chatRoomId.matches(/.*" + auth.uid + ".*/)",
            ".write": "$chatRoomId.matches(/.*" + auth.uid + ".*/)"
          }
        },
        "read": {
          "$messageId": {
            ".read": "$chatRoomId.matches(/.*" + auth.uid + ".*/)",
            ".write": "$chatRoomId.matches(/.*" + auth.uid + ".*/)"
          }
        }
      }
    }
  }
}
```

## 📁 Cấu trúc dự án

```
app/src/main/java/com/example/messengerprm/
├── MainActivity.java           # Màn hình chính hiển thị danh sách bạn bè
├── login.java                 # Màn hình đăng nhập
├── register.java              # Màn hình đăng ký với upload ảnh
├── splash.java                # Màn hình splash
├── chatWin.java              # Màn hình chat với gửi ảnh
├── SettingsActivity.java      # Màn hình cài đặt và chỉnh sửa profile
├── ImageViewerActivity.java   # Xem ảnh fullscreen
├── Users.java                # Model class cho user data
├── msgModelclass.java        # Model class cho tin nhắn
├── UserAdpter.java           # Adapter cho danh sách người dùng
├── messagesAdpter.java       # Adapter cho tin nhắn chat
├── FriendRequestAdapter.java  # Adapter cho lời mời kết bạn
├── ImageUtils.java           # Utility class xử lý ảnh Base64
└── ZoomableImageView.java    # Custom view zoom ảnh
```

## 🗄️ Cấu trúc Database

### Users Collection
```json
{
  "user": {
    "USER_ID": {
      "userId": "USER_ID",
      "userName": "Tên người dùng",
      "mail": "email@example.com",
      "password": "password",
      "profilepic": "Base64_string_hoặc_URL",
      "status": "Available"
    }
  },
  "friendRequests": {
    "toUserId": {
      "fromUserId": true
    }
  },
  "friends": {
    "userId1": {
      "userId2": true
    }
  },
  "chats": {
    "chatRoomId": {
      "messages": {
        "messageId": {
          "message": "Nội dung tin nhắn",
          "senderid": "USER_ID",
          "timeStamp": 1234567890,
          "imageUrl": "Base64_string",
          "messageType": "text|image"
        }
      },
      "read": {
        "messageId": true
      }
    }
  }
}
```

### 📸 Lưu trữ ảnh
- **Base64**: Ảnh được chuyển thành Base64 string và lưu trực tiếp trong database
- **Nén ảnh**: Tự động nén ảnh xuống 512px để tiết kiệm dung lượng
- **Chất lượng**: JPEG với chất lượng 50% để giảm kích thước

## 🚀 Cách chạy ứng dụng

1. **Clone project**
   ```bash
   git clone <repository-url>
   cd MessengerPRM
   ```

2. **Thêm file google-services.json**
   - Tải file từ Firebase Console
   - Đặt vào thư mục `app/`

3. **Build và chạy**
   ```bash
   ./gradlew build
   ```

## 📱 Luồng hoạt động

1. **Splash Screen** → 4 giây delay
2. **Login Screen** → Đăng nhập hoặc chuyển sang Register
3. **Register Screen** → Đăng ký tài khoản mới
4. **Main Activity** → Hiển thị danh sách bạn bè và tìm kiếm
5. **Settings** → Chỉnh sửa profile, quản lý bạn bè
6. **Chat** → Nhắn tin và gửi ảnh với bạn bè

## 🔒 Bảo mật

- Sử dụng Firebase Authentication
- Dữ liệu được bảo vệ bởi Security Rules
- Chỉ user đã đăng nhập mới có thể đọc/ghi dữ liệu của mình
- Chỉ bạn bè mới nhắn tin được

## 📊 Firebase Services sử dụng

- ✅ **Firebase Authentication** - Xác thực người dùng
- ✅ **Firebase Realtime Database** - Lưu trữ dữ liệu
- ❌ **Firebase Storage** - Không sử dụng (free tier)

## 🎯 Tính năng mới: Chỉnh sửa Profile

### Các tính năng:
- ✅ **Chỉnh sửa tên**: Thay đổi tên hiển thị
- ✅ **Cập nhật trạng thái**: Thay đổi status cá nhân
- ✅ **Thay đổi password**: Cập nhật mật khẩu (tùy chọn)
- ✅ **Đổi avatar**: Upload ảnh đại diện mới
- ✅ **Validation**: Kiểm tra dữ liệu trước khi lưu
- ✅ **Real-time update**: Cập nhật ngay lập tức
- ✅ **Firebase Auth sync**: Đồng bộ password với Firebase Authentication

### Validation Rules:
- **Tên**: Bắt buộc, tối thiểu 2 ký tự
- **Trạng thái**: Bắt buộc, tối đa 100 ký tự
- **Password**: Tùy chọn, tối thiểu 6 ký tự nếu thay đổi
- **Confirm Password**: Phải khớp với password mới
- **Email**: Không thể chỉnh sửa (bảo mật)

## 🛠️ Công nghệ sử dụng

- **Android SDK** - Phát triển ứng dụng
- **Java** - Ngôn ngữ lập trình
- **Firebase** - Backend services
- **Material Design** - UI/UX
- **CircleImageView** - Hiển thị ảnh profile tròn
- **RecyclerView** - Hiển thị danh sách
- **Picasso** - Load ảnh từ URL

## 📞 Hỗ trợ

Nếu có vấn đề gì, hãy kiểm tra:
1. File `google-services.json` đã được thêm chưa
2. Firebase project đã được cấu hình đúng chưa
3. Internet connection có ổn định không
4. Permissions đã được cấp chưa (camera, storage)

## 📚 Tài liệu tham khảo

- [README_IMAGE_FEATURES.md](README_IMAGE_FEATURES.md) - Tính năng gửi hình ảnh
- [README_PROFILE_EDIT.md](README_PROFILE_EDIT.md) - Tính năng chỉnh sửa profile 