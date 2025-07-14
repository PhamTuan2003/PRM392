# Tính năng Chỉnh sửa Profile trong Settings

## Tổng quan
Đã thêm tính năng chỉnh sửa thông tin cá nhân trong màn hình Settings, cho phép người dùng cập nhật tên, trạng thái, password và avatar.

## Các tính năng mới

### 1. Chỉnh sửa thông tin cá nhân
- **Tên người dùng**: Có thể thay đổi tên hiển thị
- **Email**: Hiển thị email (chỉ đọc, không thể sửa)
- **Trạng thái**: Cập nhật status cá nhân
- **Password**: Thay đổi mật khẩu (tùy chọn)
- **Avatar**: Thay đổi ảnh đại diện

### 2. Validation dữ liệu
- **Tên người dùng**: Bắt buộc, tối thiểu 2 ký tự
- **Trạng thái**: Bắt buộc, tối đa 100 ký tự
- **Password**: Tùy chọn, tối thiểu 6 ký tự nếu thay đổi
- **Confirm Password**: Phải khớp với password mới
- **Email**: Không thể chỉnh sửa (bảo mật)

### 3. Giao diện cải tiến
- **Material Design**: Sử dụng TextInputLayout với outline style
- **Password Toggle**: Hiển thị/ẩn password
- **ScrollView**: Hỗ trợ cuộn khi nội dung dài
- **Loading state**: Hiển thị trạng thái đang lưu
- **Error handling**: Thông báo lỗi rõ ràng

## Cấu trúc dữ liệu

### Layout mới (activity_settings.xml)
```xml
<ScrollView>
    <LinearLayout>
        <!-- Header -->
        <TextView>Chỉnh sửa Profile</TextView>
        
        <!-- Avatar section -->
        <LinearLayout>
            <CircleImageView />
            <Button>Đổi avatar</Button>
        </LinearLayout>
        
        <!-- Profile info section -->
        <LinearLayout>
            <TextInputLayout>
                <TextInputEditText>userName</TextInputEditText>
            </TextInputLayout>
            <TextInputLayout>
                <TextInputEditText>email (disabled)</TextInputEditText>
            </TextInputLayout>
            <TextInputLayout>
                <TextInputEditText>status</TextInputEditText>
            </TextInputLayout>
            <TextInputLayout passwordToggleEnabled="true">
                <TextInputEditText>newPassword</TextInputEditText>
            </TextInputLayout>
            <TextInputLayout passwordToggleEnabled="true">
                <TextInputEditText>confirmPassword</TextInputEditText>
            </TextInputLayout>
            <Button>Lưu thay đổi</Button>
        </LinearLayout>
        
        <!-- Existing sections -->
        <FriendList />
        <FriendRequests />
        <ThemeToggle />
        <Logout />
    </LinearLayout>
</ScrollView>
```

## Logic xử lý

### SettingsActivity.java
```java
// Load profile data
private void loadUserProfile() {
    // Load userName, email, status from Firebase
    // Password fields left empty for security
    // Populate TextInputEditText fields
}

// Save profile changes
private void saveProfile() {
    // Validate input (name, status, password)
    // Show loading state
    // Update Firebase database
    // Update Firebase Auth password if changed
    // Show success/error message
    // Clear password fields after success
}
```

## String Resources mới

### strings.xml
```xml
<string name="edit_profile">Chỉnh sửa Profile</string>
<string name="personal_info">Thông tin cá nhân</string>
<string name="user_name">Tên người dùng</string>
<string name="email">Email</string>
<string name="status">Trạng thái</string>
<string name="new_password">Mật khẩu mới</string>
<string name="confirm_password">Xác nhận mật khẩu</string>
<string name="save_changes">Lưu thay đổi</string>
<string name="saving">Đang lưu...</string>
<string name="change_avatar">Đổi avatar</string>
<string name="profile_updated">Cập nhật profile thành công!</string>
<string name="profile_update_error">Lỗi cập nhật profile!</string>
<string name="password_updated">Cập nhật mật khẩu thành công!</string>
<string name="password_update_error">Lỗi cập nhật mật khẩu!</string>
<string name="name_required">Tên người dùng không được để trống</string>
<string name="name_too_short">Tên người dùng phải có ít nhất 2 ký tự</string>
<string name="status_required">Trạng thái không được để trống</string>
<string name="status_too_long">Trạng thái không được quá 100 ký tự</string>
<string name="password_required">Mật khẩu không được để trống</string>
<string name="password_too_short">Mật khẩu phải có ít nhất 6 ký tự</string>
<string name="password_not_match">Mật khẩu xác nhận không khớp</string>
<string name="password_unchanged">Mật khẩu không thay đổi</string>
```

## Cách sử dụng

### 1. Truy cập Settings
- Nhấn nút Settings (⚙️) trong MainActivity
- Màn hình Settings sẽ hiển thị với thông tin profile hiện tại

### 2. Chỉnh sửa thông tin
- **Tên**: Nhập tên mới (tối thiểu 2 ký tự)
- **Trạng thái**: Nhập status mới (tối đa 100 ký tự)
- **Password**: Nhập password mới (tùy chọn, tối thiểu 6 ký tự)
- **Confirm Password**: Nhập lại password mới để xác nhận
- **Avatar**: Nhấn "Đổi avatar" để chọn ảnh mới

### 3. Lưu thay đổi
- Nhấn "Lưu thay đổi"
- Hệ thống sẽ validate và lưu vào Firebase
- Nếu thay đổi password, sẽ cập nhật cả Firebase Auth và Database
- Hiển thị thông báo thành công/lỗi
- Xóa trường password sau khi lưu thành công

## Validation Rules

### Tên người dùng
- ✅ Bắt buộc nhập
- ✅ Tối thiểu 2 ký tự
- ✅ Không có ký tự đặc biệt

### Trạng thái
- ✅ Bắt buộc nhập
- ✅ Tối đa 100 ký tự
- ✅ Hỗ trợ nhiều dòng

### Password (tùy chọn)
- ❌ Không bắt buộc (có thể để trống)
- ✅ Nếu nhập thì tối thiểu 6 ký tự
- ✅ Phải khớp với confirm password
- ✅ Có toggle hiển thị/ẩn password

### Email
- ❌ Không thể chỉnh sửa (bảo mật)
- ✅ Hiển thị email hiện tại

## Firebase Database Update

### Cấu trúc dữ liệu
```json
{
  "user": {
    "USER_ID": {
      "userName": "Tên mới",
      "mail": "email@example.com",
      "status": "Trạng thái mới",
      "password": "password_mới_nếu_thay_đổi",
      "profilepic": "Base64_string"
    }
  }
}
```

### Firebase Auth Update
- Cập nhật password trong Firebase Authentication
- Đồng bộ với password trong Database
- Xử lý lỗi nếu Firebase Auth update thất bại

## Lưu ý quan trọng

1. **Bảo mật**: 
   - Email không thể chỉnh sửa để tránh xung đột với Firebase Auth
   - Password hiện tại không hiển thị vì lý do bảo mật
   - Password được cập nhật cả Firebase Auth và Database

2. **Real-time**: Thay đổi sẽ được cập nhật ngay lập tức trong toàn bộ ứng dụng

3. **Validation**: Kiểm tra dữ liệu trước khi lưu

4. **User Experience**: 
   - Loading state và thông báo rõ ràng
   - Xóa trường password sau khi lưu thành công
   - Password toggle để dễ nhập

5. **Material Design**: Giao diện đẹp và thân thiện

## Troubleshooting

### Lỗi thường gặp:
1. **Validation failed**: Kiểm tra lại yêu cầu về độ dài và nội dung
2. **Save failed**: Kiểm tra kết nối internet và Firebase rules
3. **Password update failed**: Kiểm tra Firebase Auth configuration
4. **Avatar not loading**: Kiểm tra quyền truy cập storage

### Firebase Rules (khuyến nghị):
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
        },
        "password": {
          ".validate": "newData.isString() && newData.val().length >= 6"
        }
      }
    }
  }
}
```

## Tính năng bảo mật

### Password Security
- **Không hiển thị password cũ**: Bảo mật thông tin
- **Validation chặt chẽ**: Tối thiểu 6 ký tự
- **Confirm password**: Tránh nhập sai
- **Firebase Auth sync**: Đồng bộ với hệ thống xác thực
- **Clear after save**: Xóa trường password sau khi lưu

### Error Handling
- **Partial success**: Nếu chỉ Database update thành công
- **Clear feedback**: Thông báo rõ ràng về trạng thái
- **Field-specific errors**: Hiển thị lỗi ở đúng trường 