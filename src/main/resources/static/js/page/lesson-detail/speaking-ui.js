/* [MODULE: SPEAKING UI] XỬ LÝ GIAO DIỆN LUYỆN NÓI.
 */

document.addEventListener('DOMContentLoaded', () => {

    // 1. Elements
    const refTextEl = document.getElementById('speakingRefText');
    const hintArea = document.getElementById('hintArea');
    const resultArea = document.getElementById('speakingResultArea');

    const checkBtn = document.getElementById('checkBtn');
    const skipBtn = document.getElementById('skipBtn');
    const nextBtn2 = document.getElementById('nextBtn2');
    const replayBtn = document.getElementById('replayBtn');

    const recordBtn = document.getElementById('recordBtn');
    const recordLabel = document.querySelector('.speaking-record-label');

    // Mặc định ẩn nút Check vì Speaking dùng RecordBtn để check luôn
    if (checkBtn) checkBtn.style.display = 'none';

    // Biến cho Recording
    let mediaRecorder;
    let audioChunks = [];
    let isRecording = false;

    // 2. State Listeners
    function handleSentenceChange(index, sentence) {
        // Hiển thị câu mẫu
        if (refTextEl) {
            refTextEl.textContent = sentence.content || '';
        }

        // Reset UI
        if (resultArea) {
            resultArea.innerHTML = 'Your results will appear here';
            resultArea.className = 'speaking-result-placeholder';
        }
        if (nextBtn2) nextBtn2.style.display = 'none';
        if (replayBtn) replayBtn.style.display = 'none';
        if (skipBtn) skipBtn.style.display = '';

        // Reset Record button
        isRecording = false;
        if (recordBtn) {
            recordBtn.innerHTML = '<i class="fas fa-microphone"></i>';
            recordBtn.classList.remove('recording');
        }
        if (recordLabel) recordLabel.textContent = 'Tap to speak';

        // Hiển thị Proper Noun Hint từ Backend
        if (hintArea) {
            const nouns = sentence.properNouns || [];
            if (nouns.length > 0) {
                hintArea.innerHTML = '💡 <strong>Hint:</strong> ' + nouns.map(n => '<span class="hint-proper-noun">' + n + '</span>').join(', ');
                hintArea.style.display = 'block';
            } else {
                hintArea.style.display = 'none';
            }
        }
    }

    document.addEventListener('lesson:sentenceChanged', (e) => {
        handleSentenceChange(e.detail.index, e.detail.sentence);
    });

    // 3. UI Event Bindings
    if (nextBtn2) {
        nextBtn2.addEventListener('click', () => {
            if (window.LessonState.currentIndex < window.LessonState.sentences.length - 1) {
                window.LessonState.next();
            }
        });
    }

    if (replayBtn) {
        replayBtn.addEventListener('click', () => {
            window.LessonState.loadSentence(window.LessonState.currentIndex);
            window.LessonState.play();
        });
    }

    if (skipBtn) {
        skipBtn.addEventListener('click', () => {
            if (skipBtn) skipBtn.style.display = 'none';
            const isLastSentence = window.LessonState.currentIndex >= window.LessonState.sentences.length - 1;
            if (nextBtn2) nextBtn2.style.display = isLastSentence ? 'none' : '';
            if (replayBtn) replayBtn.style.display = '';
        });
    }

    // --- Xử lý Audio Recording ---
    if (recordBtn) {
        recordBtn.addEventListener('click', async () => {
            if (!isRecording) {
                try {
                    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

                    // Sử dụng RecordRTC thay cho MediaRecorder mặc định
                    mediaRecorder = new RecordRTC(stream, {
                        type: 'audio',
                        mimeType: 'audio/wav',
                        recorderType: RecordRTC.StereoAudioRecorder, // Ép dùng bộ mã hóa WAV
                        desiredSampRate: 16000, // 16kHz là tần số lý tưởng nhất cho Wit.ai
                        numberOfAudioChannels: 1 // Ghi âm Mono để file nhẹ hơn
                    });

                    mediaRecorder.startRecording();
                    isRecording = true;
                    recordBtn.innerHTML = '<i class="fas fa-stop"></i>';
                    recordBtn.classList.add('recording');
                    if (recordLabel) recordLabel.textContent = 'Recording... Tap to stop';

                    if (resultArea) resultArea.innerHTML = 'Listening...';

                } catch (err) {
                    console.error("Lỗi Microphone:", err);
                    alert("Không thể truy cập Microphone! Vui lòng kiểm tra quyền truy cập.");
                }
            } else {
                // Dừng ghi âm
                mediaRecorder.stopRecording(function () {
                    isRecording = false;
                    recordBtn.innerHTML = '<i class="fas fa-microphone"></i>';
                    recordBtn.classList.remove('recording');
                    if (recordLabel) recordLabel.textContent = 'Processing...';

                    // Lấy file WAV từ thư viện
                    let audioBlob = mediaRecorder.getBlob();

                    console.log("Đã tạo file WAV, kích thước:", audioBlob.size, "bytes");

                    // Gửi file lên backend
                    sendAudioToBackend(audioBlob);

                    // Tắt mic
                    mediaRecorder.stream.getTracks().forEach(track => track.stop());
                });
            }
        });
    }

    // Gửi Audio lên Spring Backend
    function sendAudioToBackend(audioBlob) {
        const formData = new FormData();
        // audio.webm (hoặc tùy định dạng mediaRecorder)
        formData.append('audio', audioBlob, 'speaking-audio.webm');
        const referenceText = refTextEl ? refTextEl.textContent : '';
        formData.append('referenceText', referenceText);

        if (resultArea) resultArea.innerHTML = '<div class="spinner"></div> Evaluating your pronunciation...';

        fetch('/api/speaking/evaluate', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (!response.ok) throw new Error("Network response was not ok");
                return response.json();
            })
            .then(data => {
                displayEvaluationResult(data);
            })
            .catch(error => {
                console.error('Error evaluating speaking:', error);
                if (resultArea) resultArea.innerHTML = '<span style="color:red">Error evaluating audio. Please try again.</span>';
                if (recordLabel) recordLabel.textContent = 'Tap to speak again';
            });
    }

    function displayEvaluationResult(data) {
        if (!resultArea) return;

        // data = { referenceText, transcribedText, score, feedback }
        let colorClass = data.score >= 80 ? 'text-success' : (data.score >= 50 ? 'text-warning' : 'text-danger');

        let html = `
            <div style="font-size: 1.1em; margin-bottom: 10px;">
                <strong>You said:</strong> <span style="color: #555;">${data.transcribedText || '(Nothing detected)'}</span>
            </div>
            <div style="font-size: 1.2em; font-weight: bold; margin-bottom: 10px;" class="${colorClass}">
                Score: ${data.score}%
            </div>
            <div style="font-style: italic;">${data.feedback}</div>
        `;

        resultArea.innerHTML = html;
        resultArea.className = 'speaking-result-box'; // remove placeholder styling

        if (recordLabel) recordLabel.textContent = 'Tap to try again';

        if (skipBtn) skipBtn.style.display = 'none';
        const isLastSentence = window.LessonState.currentIndex >= window.LessonState.sentences.length - 1;
        if (nextBtn2) nextBtn2.style.display = isLastSentence ? 'none' : '';
        if (replayBtn) replayBtn.style.display = '';
    }

    // 4. Proactive Init
    if (window.LessonState && window.LessonState.sentences && window.LessonState.sentences.length > 0) {
        const curIdx = window.LessonState.currentIndex || 0;
        handleSentenceChange(curIdx, window.LessonState.sentences[curIdx]);
    }
});