# Tính năng Message Scheduling

## Tổng quan
Tính năng Message Scheduling cho phép người dùng lên lịch gửi tin nhắn tự động tại một thời điểm cụ thể trong tương lai.

## Các tính năng chính

### 1. Lên lịch tin nhắn
- Người dùng có thể lên lịch gửi tin nhắn văn bản
- Chọn ngày và giờ gửi tin nhắn
- Hệ thống sẽ tự động gửi tin nhắn khi đến thời gian đã lên lịch

### 2. Quản lý tin nhắn đã lên lịch
- Xem danh sách tất cả tin nhắn đã lên lịch
- Hủy lịch tin nhắn chưa được gửi
- Theo dõi trạng thái tin nhắn (chờ gửi, đã gửi, lỗi)

### 3. Service tự động
- MessageSchedulerService chạy nền để kiểm tra và gửi tin nhắn theo lịch
- Sử dụng AlarmManager để đảm bảo tin nhắn được gửi đúng thời gian
- Lưu trữ dữ liệu trên Firebase Realtime Database

## Cách sử dụng

### Lên lịch tin nhắn mới
1. Mở cuộc trò chuyện với người bạn muốn gửi tin nhắn
2. Nhấn nút đồng hồ (⏰) bên cạnh nút gửi ảnh
3. Nhập nội dung tin nhắn
4. Chọn ngày và giờ gửi tin nhắn
5. Nhấn "Lên lịch" để xác nhận

### Xem tin nhắn đã lên lịch
1. Vào Settings (Cài đặt)
2. Nhấn "Tin nhắn đã lên lịch"
3. Xem danh sách tất cả tin nhắn đã lên lịch
4. Nhấn "Hủy lịch" để hủy tin nhắn chưa gửi

## Cấu trúc dữ liệu

### ScheduledMessage
```java
{
    "messageId": "unique_id",
    "message": "Nội dung tin nhắn",
    "senderId": "user_id_người_gửi",
    "receiverId": "user_id_người_nhận",
    "roomId": "room_id_cuộc_trò_chuyện",
    "type": "text",
    "scheduledTime": 1640995200000, // timestamp
    "createdAt": 1640995200000,
    "isSent": false,
    "status": "pending" // pending, sent, failed
}
```

### Database Structure
```
scheduled_messages/
├── message_id_1/
│   ├── message: "Hello"
│   ├── senderId: "user1"
│   ├── receiverId: "user2"
│   ├── roomId: "user1user2"
│   ├── type: "text"
│   ├── scheduledTime: 1640995200000
│   ├── createdAt: 1640995200000
│   ├── isSent: false
│   └── status: "pending"
└── message_id_2/
    └── ...
```

## Các file chính

### Java Classes
- `ScheduledMessage.java`: Model cho tin nhắn đã lên lịch
- `MessageSchedulerService.java`: Service xử lý gửi tin nhắn theo lịch
- `ScheduledMessagesActivity.java`: Activity hiển thị danh sách tin nhắn đã lên lịch
- `ScheduledMessagesAdapter.java`: Adapter cho RecyclerView
- `chatWin.java`: Thêm logic lên lịch tin nhắn

### Layout Files
- `dialog_schedule_message.xml`: Dialog lên lịch tin nhắn
- `activity_scheduled_messages.xml`: Layout cho danh sách tin nhắn đã lên lịch
- `item_scheduled_message.xml`: Layout cho item tin nhắn đã lên lịch

### Drawable Files
- `ic_schedule.xml`: Icon đồng hồ
- `rounded_edittext.xml`: Background cho EditText
- `rounded_button.xml`: Background cho button
- `rounded_button_cancel.xml`: Background cho button cancel

## Permissions
```xml
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## Service Registration
```xml
<service
    android:name=".MessageSchedulerService"
    android:enabled="true"
    android:exported="false" />
```

## Tính năng nâng cao

### 1. Lên lịch tin nhắn ảnh
- Mở rộng để hỗ trợ lên lịch gửi ảnh
- Lưu trữ ảnh dưới dạng Base64

### 2. Lên lịch tin nhắn định kỳ
- Gửi tin nhắn lặp lại theo ngày/tuần/tháng
- Tùy chọn số lần lặp lại

### 3. Template tin nhắn
- Tạo sẵn các mẫu tin nhắn thường dùng
- Chọn template và chỉnh sửa nội dung

### 4. Thông báo nhắc nhở
- Gửi thông báo trước khi tin nhắn được gửi
- Cho phép người dùng hủy hoặc chỉnh sửa

## Lưu ý kỹ thuật

1. **Battery Optimization**: Service có thể bị tắt bởi hệ thống để tiết kiệm pin
2. **Network Connectivity**: Cần kết nối internet để gửi tin nhắn
3. **Time Zone**: Thời gian được lưu dưới dạng timestamp UTC
4. **Data Persistence**: Dữ liệu được lưu trên Firebase để đảm bảo không mất khi app bị tắt

## Troubleshooting

### Tin nhắn không được gửi
1. Kiểm tra kết nối internet
2. Kiểm tra quyền SCHEDULE_EXACT_ALARM
3. Kiểm tra battery optimization settings
4. Restart app và service

### Service không hoạt động
1. Kiểm tra AndroidManifest.xml đã đăng ký service
2. Kiểm tra permissions
3. Restart device nếu cần thiết

## Future Improvements

1. **Push Notifications**: Sử dụng Firebase Cloud Messaging
2. **Background Processing**: Sử dụng WorkManager thay vì AlarmManager
3. **Offline Support**: Lưu tin nhắn local khi không có internet
4. **Analytics**: Theo dõi hiệu suất và lỗi
5. **User Preferences**: Tùy chỉnh thời gian nhắc nhở 