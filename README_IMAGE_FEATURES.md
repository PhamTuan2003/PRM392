# Tính năng Gửi Hình Ảnh trong Messenger PRM

## Tổng quan
Đã thêm tính năng gửi hình ảnh và chụp ảnh vào ứng dụng chat, tương tự như Messenger của Meta.

## Các tính năng mới

### 1. Chụp ảnh trực tiếp
- Nhấn nút camera (📷) để chụp ảnh trực tiếp
- Ảnh sẽ được lưu tạm thời và upload lên Firebase Storage
- Sau khi upload thành công, ảnh sẽ được gửi trong chat

### 2. Chọn ảnh từ thư viện
- Nhấn nút gallery (🖼️) để chọn ảnh từ thư viện
- Hỗ trợ chọn ảnh từ thư viện của thiết bị
- Ảnh sẽ được upload và gửi trong chat

### 3. Hiển thị hình ảnh trong chat
- Hình ảnh được hiển thị với kích thước 200x200dp
- Sử dụng scaleType="centerCrop" để hiển thị đẹp
- Có placeholder và error image khi load ảnh

## Cấu trúc dữ liệu mới

### msgModelclass
```java
public class msgModelclass {
    String message;        // Nội dung tin nhắn text
    String senderid;       // ID người gửi
    long timeStamp;        // Thời gian gửi
    String imageUrl;       // URL hình ảnh (mới)
    String messageType;    // Loại tin nhắn: "text" hoặc "image" (mới)
}
```

## Permissions đã thêm

### AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```

### FileProvider
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## Dependencies mới

### build.gradle.kts
```kotlin
implementation(libs.firebase.storage)
```

## Cách sử dụng

### 1. Gửi tin nhắn text
- Nhập tin nhắn vào ô text
- Nhấn nút gửi (➤)

### 2. Chụp và gửi ảnh
- Nhấn nút camera (📷)
- Chụp ảnh
- Ảnh sẽ tự động upload và gửi

### 3. Chọn và gửi ảnh từ thư viện
- Nhấn nút gallery (🖼️)
- Chọn ảnh từ thư viện
- Ảnh sẽ tự động upload và gửi

## Lưu ý quan trọng

1. **Permissions**: Ứng dụng sẽ yêu cầu quyền truy cập camera và storage khi lần đầu sử dụng
2. **Firebase Storage**: Cần cấu hình Firebase Storage trong project Firebase
3. **Network**: Cần kết nối internet để upload ảnh
4. **Storage**: Ảnh được lưu trong thư mục `chat_images/{room_id}/` trên Firebase Storage

## Cấu trúc Firebase Storage
```
chat_images/
├── {senderRoom}/
│   ├── image_20241201_143022.jpg
│   ├── image_20241201_143045.jpg
│   └── ...
```

## Troubleshooting

### Lỗi thường gặp:
1. **Permission denied**: Cấp quyền camera và storage trong Settings
2. **Upload failed**: Kiểm tra kết nối internet và Firebase Storage rules
3. **Image not loading**: Kiểm tra URL ảnh và network connection

### Firebase Storage Rules (khuyến nghị):
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /chat_images/{roomId}/{imageId} {
      allow read, write: if request.auth != null;
    }
  }
}
``` 