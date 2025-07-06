# Medicine Time - Android App

## Mô tả
Medicine Time là một ứng dụng Android native được thiết kế để giúp người dùng nhớ uống thuốc hàng ngày. Ứng dụng cho phép người dùng lưu trữ thông tin thuốc và thiết lập nhiều báo thức cho từng loại thuốc.

## Tính năng chính
- Lưu trữ thông tin thuốc (pills)
- Thiết lập nhiều báo thức cho mỗi loại thuốc
- Xem thuốc theo ngày (today view)
- Chọn ngày để xem lịch sử thuốc
- Lưu trữ lịch sử khi nào thuốc được uống
- Báo cáo hàng tháng về việc sử dụng thuốc

## Yêu cầu hệ thống
- Android Studio Hedgehog | 2023.1.1 hoặc mới hơn
- Android SDK 34
- Java 8 hoặc mới hơn
- Gradle 8.5

## Cài đặt và chạy

### 1. Clone project
```bash
git clone <repository-url>
cd Medicine-Time-Android-App-Project-master
```

### 2. Mở project trong Android Studio
- Mở Android Studio
- Chọn "Open an existing Android Studio project"
- Chọn thư mục project

### 3. Sync project
- Đợi Android Studio sync project với Gradle
- Nếu có lỗi, hãy chọn "Sync Project with Gradle Files"

### 4. Build và chạy
- Chọn device hoặc emulator
- Nhấn "Run" (Shift + F10)

## Cấu trúc project
```
app/
├── src/main/
│   ├── java/com/vishwajeeth/medicinetime/
│   │   ├── addmedicine/     # Thêm thuốc mới
│   │   ├── alarm/          # Báo thức
│   │   ├── data/           # Data layer
│   │   ├── medicine/       # Danh sách thuốc
│   │   ├── report/         # Báo cáo
│   │   └── utils/          # Utilities
│   ├── res/                # Resources
│   └── AndroidManifest.xml
```

## Công nghệ sử dụng
- MVP Architecture Pattern
- AndroidX
- Material Design Components
- Compact Calendar View
- View Binding
- SQLite Database

## Flavors
- **mock**: Phiên bản test với dữ liệu giả
- **prod**: Phiên bản production

## License
MIT License



