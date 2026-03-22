import os
import re
import mysql.connector
import sys
import io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# ==========================================
# CẤU HÌNH DATABASE & ĐƯỜNG DẪN
# ==========================================
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '123456',
    'database': 'english_learning_db'
}

DATASET_PATH = r"D:\EnglishListeningDataset"
BASE_CLOUD_FOLDER = "EnglishListeningDataset"
CLOUD_NAME = "dzuk9tt0a"

def get_cloud_url(rel_path_str, is_video=True):
    # Cloudinary phân biệt video(audio) và image
    resource_type = "video" if is_video else "image"
    # Chuyển dấu \ thành /
    cloud_path = f"{BASE_CLOUD_FOLDER}/{rel_path_str.replace(os.sep, '/')}"
    # Mã hóa URL khoảng trắng
    cloud_path = cloud_path.replace(" ", "%20")
    return f"https://res.cloudinary.com/{CLOUD_NAME}/{resource_type}/upload/{cloud_path}"

def natural_sort_key(s):
    return [int(text) if text.isdigit() else text.lower() for text in re.split(r'(\d+)', s)]

def add_spaces(s):
    s = re.sub(r'([a-z])([A-Z])', r'\1 \2', s)
    s = re.sub(r'([A-Z]+)([A-Z][a-z])', r'\1 \2', s)
    s = re.sub(r'([a-zA-Z])(\d)', r'\1 \2', s)
    s = re.sub(r'(\d)([a-zA-Z])', r'\1 \2', s)
    return re.sub(r'\s+', ' ', s).strip()

def main():
    print("🚀 Bắt đầu quét thư mục và đồng bộ DB...")
    
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
    except Exception as e:
        print(f"❌ Lỗi kết nối CSDL: {e}")
        return

    # Xóa sạch data cũ (TRUNCATE) để chèn 40,000 dòng sạch sẽ
    print("🧹 Đang dọn dẹp Database cũ...")
    cursor.execute("SET FOREIGN_KEY_CHECKS = 0;")
    cursor.execute("TRUNCATE TABLE sentences;")
    cursor.execute("TRUNCATE TABLE lessons;")
    cursor.execute("TRUNCATE TABLE sections;")
    cursor.execute("TRUNCATE TABLE categories;")
    cursor.execute("SET FOREIGN_KEY_CHECKS = 1;")
    conn.commit()

    # Dùng os.listdir thay vì os.walk để kiểm soát từng cấp độ
    # Cấp 1: CATEGORY
    cat_dirs = sorted([d for d in os.listdir(DATASET_PATH) if os.path.isdir(os.path.join(DATASET_PATH, d))], key=natural_sort_key)
    
    for cat_name in cat_dirs:
        cat_path = os.path.join(DATASET_PATH, cat_name)
        
        # Bóc tách tên và Level (VD: "ShortStories (a1-c1)")
        match_level = re.search(r'\((.*?)\)', cat_name)
        level_range = match_level.group(1).upper() if match_level else ""
        clean_name = cat_name.split('(')[0].strip()
        clean_name = add_spaces(clean_name)
        
        # Construct cover URL
        rel_cover = os.path.relpath(os.path.join(cat_path, "cover.jpg"), DATASET_PATH)
        cover_url = get_cloud_url(rel_cover, is_video=False)

        cursor.execute(
            "INSERT INTO categories (name, image_url, level_range, total_lessons, description) VALUES (%s, %s, %s, %s, %s)",
            (clean_name, cover_url, level_range, 0, f"{clean_name} Practice")
        )
        cat_id = cursor.lastrowid
        
        print(f"📂 Đã Insert Category: {clean_name} (ID={cat_id})")
        total_lessons_in_cat = 0

        # Cấp 2: SECTION
        sec_dirs = sorted([d for d in os.listdir(cat_path) if os.path.isdir(os.path.join(cat_path, d))], key=natural_sort_key)
        for sec_name in sec_dirs:
            sec_path = os.path.join(cat_path, sec_name)
            
            cursor.execute(
                "INSERT INTO sections (category_id, name, description) VALUES (%s, %s, %s)",
                (cat_id, sec_name, f"{sec_name} Exercises")
            )
            sec_id = cursor.lastrowid
            
            # Cấp 3: LESSON
            lesson_dirs = sorted([d for d in os.listdir(sec_path) if os.path.isdir(os.path.join(sec_path, d))], key=natural_sort_key)
            for lesson_name in lesson_dirs:
                lesson_path = os.path.join(sec_path, lesson_name)
                
                match_les_level = re.search(r'\((.*?)\)', lesson_name)
                les_level = match_les_level.group(1).upper() if match_les_level else level_range
                les_clean_name = lesson_name.split('(')[0].strip()
                les_clean_name = re.sub(r'^\d+_', '', les_clean_name).replace('_', ' ')
                les_clean_name = add_spaces(les_clean_name)
                
                # Quét số file audio
                audio_files = [f for f in os.listdir(lesson_path) if f.endswith('.mp3')]
                # Sắp xếp đúng theo số 1.mp3, 2.mp3...
                audio_files.sort(key=lambda x: int(x.split('.')[0]) if x.split('.')[0].isdigit() else 999)
                total_audio = len(audio_files)
                
                cursor.execute(
                    "INSERT INTO lessons (section_id, title, level, total_sentences) VALUES (%s, %s, %s, %s)",
                    (sec_id, les_clean_name, les_level, total_audio)
                )
                lesson_id = cursor.lastrowid
                total_lessons_in_cat += 1
                
                # Đọc Transcript
                txt_path = os.path.join(lesson_path, "transcript.txt")
                transcript_lines = []
                if os.path.exists(txt_path):
                    with open(txt_path, 'r', encoding='utf-8') as f:
                        transcript_lines = [line.strip() for line in f.readlines() if line.strip()]
                
                # Nạp Cấp 4: SENTENCES
                sentence_inserts = []
                last_txt_index = 0
                for idx, audio_f in enumerate(audio_files):
                    order_idx = idx + 1
                    rel_audio = os.path.relpath(os.path.join(lesson_path, audio_f), DATASET_PATH)
                    audio_url = get_cloud_url(rel_audio, is_video=True)
                    
                    # Logic Khớp Transcript (Khá quan trọng do dính lỗi 1 câu thành 2 dòng như bạn nói)
                    # Cách "Best Effort": Mỗi 1 mp3 bốc tạm 1 dòng, nếu thiếu thì cho rỗng.
                    # Khuyến nghị tương lai: User nên sửa lại Python Crawler ghép nối HTML.
                    snippet = ""
                    if last_txt_index < len(transcript_lines):
                        snippet = transcript_lines[last_txt_index]
                        last_txt_index += 1
                    
                    # Nếu là file audio cuối cùng mà text vẫn còn dư, ghép dồn tất cả text còn lại vào câu cuối cùng
                    if order_idx == total_audio and last_txt_index < len(transcript_lines):
                        snippet += " " + " ".join(transcript_lines[last_txt_index:])
                        
                    sentence_inserts.append((lesson_id, audio_url, snippet, order_idx))
                
                if sentence_inserts:
                    cursor.executemany(
                        "INSERT INTO sentences (lesson_id, audio_url, content, order_index) VALUES (%s, %s, %s, %s)",
                        sentence_inserts
                    )

        # Cập nhật tổng số Lesson cho Category
        cursor.execute("UPDATE categories SET total_lessons = %s WHERE id = %s", (total_lessons_in_cat, cat_id))
        conn.commit()

    cursor.close()
    conn.close()
    print("🎉 HOÀN TẤT! ĐÃ NẠP TOÀN BỘ DATASET (KỂ CẢ TRANSCRIPT) VÀO MYSQL.")
    
if __name__ == "__main__":
    main()
