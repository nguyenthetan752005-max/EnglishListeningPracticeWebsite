# Hướng dẫn Cài đặt Cơ sở Dữ liệu

Đây là ứng dụng Java Spring Boot kết nối với CSDL MySQL. Bạn cần làm 3 bước siêu ngắn gọn sau để có một Database đầy đủ 40,000 bài tập trên máy cá nhân:

## 1. Tạo Database trống
Mở ứng dụng quản lý MySQL (XAMPP, MySQL Workbench, DBeaver, Command Line) và chạy đoạn lệnh này:
```sql
CREATE DATABASE english_learning_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 2. Nạp dữ liệu (Import SQL)
Cả bộ dữ liệu đã được nén hết trong file `backup_project_english.sql` đi kèm dự án. 
Bạn mở MySQL quản lý của máy bạn lên, chọn tính năng **Data Import**, trỏ đường dẫn trích xuất tới file này và đổ toàn bộ Database vào trong DataBase `english_learning_db` vừa tạo.

## 3. Sửa cấu hình (Password) 
Mở file Source Code của dự án Java: `src/main/resources/application.properties`
Tìm và sửa mật khẩu thành mật khẩu MySQL chính xác trên máy của bạn:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/english_learning_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root        # Thay bằng username MySQL máy bạn
spring.datasource.password=123456      # Thay bằng mật khẩu MySQL máy bạn
```

**Hoàn tất!** Giờ bạn chỉ cần Run hệ thống Spring Boot (file `EnglishLearningProjectApplication.java`) và tận hưởng thành quả! Mọi thứ đã hoạt động!
