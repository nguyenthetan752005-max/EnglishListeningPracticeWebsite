# Object Diagram

Các sơ đồ trong thư mục này mô tả trạng thái đối tượng cụ thể của hệ thống tại một thời điểm chạy:

- `object_diagram_overview.puml`: sơ đồ tổng quan gom các object quan trọng nhất của toàn hệ thống.
- `object_browse_content.puml`: khách duyệt category, section, lesson, sentence.
- `object_auth_login.puml`: đăng nhập thường và đăng nhập Google OAuth2.
- `object_dictation_flow.puml`: người dùng làm một câu dictation và nhận kết quả chấm.
- `object_speaking_evaluation.puml`: một lần speaking được chấm và lưu current/best result.
- `object_comment_moderation.puml`: luồng comment, reply, vote và admin ẩn bình luận.
- `object_password_reset.puml`: quên mật khẩu, tạo token và gửi email reset.
- `object_admin_content_management.puml`: admin tạo mới category, section, lesson, sentence.
- `object_tracking_leaderboard.puml`: tracking thời gian học và dữ liệu leaderboard.

Khi báo cáo, nên dùng Object Diagram để giải thích `instance thực tế`, còn Class Diagram để giải thích `cấu trúc tổng quát`.
