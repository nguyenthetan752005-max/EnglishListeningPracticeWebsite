from flask import Flask, request, jsonify
import whisper
import os

app = Flask(__name__)

#Mở Terminal của CMD/Shell ra > Chạy pip install openai-whisper để tải Whisper local
#Mở Terminal của CMD/Shell ra > cd đến project > chạy python whisper_server.py
#Giữ cửa sổ Terminal mở, xong chạy project


print("Loading Whisper model...")
model = whisper.load_model("base")
print("Model loaded successfully!")

@app.route('/transcribe', methods=['POST'])
def transcribe():
    if 'file' not in request.files:
        return jsonify({"error": "No file part"}), 400
    
    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400
    
    # Lưu file âm thanh tạm thời
    temp_path = "temp_audio_" + file.filename
    file.save(temp_path)
    
    try:
        # Xử lý nhận dạng giọng nói với Whisper (ép cứng tiếng anh để chạy nhanh hơn)
        result = model.transcribe(temp_path, language="en")
        text = result["text"]
        return jsonify({"text": text.strip()})
    except Exception as e:
        print("Error during transcription:", e)
        return jsonify({"error": str(e)}), 500
    finally:
        # Xóa file tạm sau khi đã nhận dạng xong
        if os.path.exists(temp_path):
            os.remove(temp_path)

if __name__ == '__main__':
    # Chạy Flask Server trên cổng 5000
    app.run(host='127.0.0.1', port=5000)
