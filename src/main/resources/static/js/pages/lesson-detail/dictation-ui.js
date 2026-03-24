/* [MODULE 2: GIAO DIỆN CHÉP CHÍNH TẢ] XỬ LÝ TAB DICTATION.
 * - Gọi API Backend /api/dictation/check để chấm điểm từng từ.
 * - Hiển thị gợi ý: từ đã đúng = đen, từ mới gợi ý = XANH, phần còn lại = ***.
 * - KHÔNG tô đỏ viền input khi sai.
 * - Hiện ⚠ Incorrect khi sai, ✅ You are correct! khi đúng.
 * - Nút Next + Replay sau khi hoàn thành.
 */

document.addEventListener('DOMContentLoaded', () => {

    const counterEl = document.getElementById('currentSentenceNum');
    const inputEl = document.getElementById('dictationInput');
    const feedbackEl = document.getElementById('feedbackArea');
    const pronunciationArea = document.getElementById('pronunciationArea');
    const pronunciationText = document.getElementById('pronunciationText');
    const playBtn = document.getElementById('dictPlayBtn');
    const timeDisplay = document.getElementById('dictTimeDisplay');
    const progressFill = document.getElementById('dictProgressFill');
    const progressWrapper = document.getElementById('dictProgressWrapper');
    const volumeBtn = document.getElementById('dictVolumeBtn');
    const speedSelect = document.getElementById('dictSpeedSelect');
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const checkBtn = document.getElementById('checkBtn');
    const skipBtn = document.getElementById('skipBtn');
    const nextBtn2 = document.getElementById('nextBtn2');
    const replayBtn = document.getElementById('replayBtn');

    function updateHeaderNextButtonByPosition() {
        const isLastSentence = window.LessonState.currentIndex >= window.LessonState.sentences.length - 1;
        if (nextBtn) nextBtn.style.display = isLastSentence ? 'none' : '';
    }

    // --- State Listeners ---
    document.addEventListener('lesson:sentenceChanged', (e) => {
        if (counterEl) counterEl.textContent = e.detail.index + 1;

        // Reset toàn bộ
        if (inputEl) {
            inputEl.value = '';
            inputEl.disabled = false;
        }
        if (feedbackEl) {
            feedbackEl.style.display = 'none';
            feedbackEl.innerHTML = '';
            feedbackEl.className = 'feedback-area';
        }
        const commentsSec = document.getElementById('lessonComments');
        if (commentsSec) commentsSec.style.display = 'none';
        
        if (pronunciationArea) pronunciationArea.style.display = 'none';
        if (nextBtn2) nextBtn2.style.display = 'none';
        if (replayBtn) replayBtn.style.display = 'none';
        if (checkBtn) checkBtn.style.display = '';
        if (skipBtn) skipBtn.style.display = '';
        if (progressFill) progressFill.style.width = '0%';
        if (timeDisplay) timeDisplay.textContent = '0:00 / 0:00';
        updateHeaderNextButtonByPosition();
    });

    document.addEventListener('lesson:playState', (e) => {
        if (playBtn) {
            playBtn.innerHTML = e.detail.isPlaying ? window.LessonState.pauseSvg : window.LessonState.playSvg;
        }
    });

    document.addEventListener('lesson:timeUpdate', (e) => {
        if (progressFill) progressFill.style.width = e.detail.percent + '%';
        if (timeDisplay) timeDisplay.textContent = e.detail.timeStr;
    });

    document.addEventListener('lesson:ended', () => {
        if (playBtn) playBtn.innerHTML = window.LessonState.playSvg;
    });

    document.addEventListener('lesson:muteChanged', (e) => {
        if (volumeBtn) {
            const icon = e.detail.muted ? 'fa-volume-mute' : 'fa-volume-up';
            volumeBtn.innerHTML = '<i class="fas ' + icon + '"></i>';
        }
    });


    // --- UI Event Bindings ---
    if (playBtn) playBtn.addEventListener('click', () => window.LessonState.togglePlay());
    if (prevBtn) prevBtn.addEventListener('click', () => window.LessonState.prev());
    if (nextBtn) nextBtn.addEventListener('click', () => window.LessonState.next());
    if (volumeBtn) volumeBtn.addEventListener('click', () => window.LessonState.toggleMute());
    
    if (speedSelect) {
        speedSelect.addEventListener('change', () => window.LessonState.setSpeed(speedSelect.value));
    }

    if (progressWrapper) {
        progressWrapper.addEventListener('click', (e) => {
            const rect = progressWrapper.getBoundingClientRect();
            window.LessonState.seekRatio((e.clientX - rect.left) / rect.width);
        });
    }

    // --- Nút Next ---
    if (nextBtn2) {
        nextBtn2.addEventListener('click', () => {
            if (window.LessonState.currentIndex < window.LessonState.sentences.length - 1) {
                window.LessonState.next();
            }
        });
    }

    // --- Nút Replay ---
    if (replayBtn) {
        replayBtn.addEventListener('click', () => {
            // Reload lại câu hiện tại (reset UI + phát lại audio)
            window.LessonState.loadSentence(window.LessonState.currentIndex);
            window.LessonState.play();
        });
    }

    /**
     * Render mảng hintWords thành HTML.
     * - Từ đã đúng (index < matchedCount): màu đen.
     * - Từ mới gợi ý (index === newHintIndex): màu XANH lá.
     * - Từ ẩn ("***"): màu xám mờ.
     */
    function renderHint(hintWords, newHintIndex) {
        return hintWords.map((word, i) => {
            if (word === '***') {
                return '<span class="hint-hidden">***</span>';
            } else if (i === newHintIndex) {
                return '<span class="hint-new">' + word + '</span>';
            } else {
                return '<span class="hint-matched">' + word + '</span>';
            }
        }).join(' ');
    }

    /**
     * Hiển thị trạng thái hoàn thành (đúng hoặc skip).
     */
    function showCompleted(result, statusClass, statusHtml) {
        feedbackEl.style.display = 'block';
        feedbackEl.className = 'feedback-area ' + statusClass;
        feedbackEl.innerHTML = statusHtml;

        // Hiện Pronunciation
        if (result.correctSentence && pronunciationArea && pronunciationText) {
            pronunciationArea.style.display = 'block';
            const words = result.correctSentence.trim().split(/\s+/);
            pronunciationText.innerHTML = words.map(w => '<span class="pron-word">' + w + '</span>').join(' ');
        }

        // Ẩn Check/Skip, hiện Next + Replay
        if (inputEl) inputEl.disabled = true;
        if (checkBtn) checkBtn.style.display = 'none';
        if (skipBtn) skipBtn.style.display = 'none';
        if (nextBtn2) {
            const isLastSentence = window.LessonState.currentIndex >= window.LessonState.sentences.length - 1;
            nextBtn2.style.display = isLastSentence ? 'none' : '';
        }
        if (replayBtn) replayBtn.style.display = '';

        // Hiện Comments Section khi ĐIỀN ĐÚNG
        const commentsSec = document.getElementById('lessonComments');
        if (commentsSec && result.correct) {
            commentsSec.style.display = 'block';
        }
    }

    // --- GỌI API BACKEND: CHECK ---
    if (checkBtn && inputEl) {
        checkBtn.addEventListener('click', async () => {
            const userInput = inputEl.value.trim();
            if (!userInput) return;

            const sentenceId = window.LessonState.sentences[window.LessonState.currentIndex].id;

            try {
                const response = await fetch('/api/dictation/check', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ sentenceId: sentenceId, userInput: userInput })
                });

                if (!response.ok) throw new Error('API error');
                const result = await response.json();

                if (result.correct) {
                    inputEl.value = result.correctSentence; // Điền lại câu gốc đúng chính tả + dấu câu
                    showCompleted(result, 'feedback-correct', '✅ You are correct!');
                } else {
                    // Hiện warning SVG icon + "Incorrect"
                    inputEl.classList.remove('correct');
                    feedbackEl.style.display = 'block';
                    feedbackEl.className = 'feedback-area feedback-incorrect';
                    feedbackEl.innerHTML = '<div class="feedback-status"><svg viewBox="0 0 24 24" width="20" height="20" fill="#ea580c" style="margin-bottom: 2px;"><path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"></path></svg> Incorrect</div>' +
                        '<div class="feedback-hint">' + renderHint(result.hintWords, result.newHintIndex) + '</div>';
                }
            } catch (err) {
                console.error('Dictation check failed:', err);
                feedbackEl.style.display = 'block';
                feedbackEl.className = 'feedback-area feedback-incorrect';
                feedbackEl.textContent = 'Error communicating with server.';
            }
        });
    }

    // --- GỌI API BACKEND: SKIP ---
    if (skipBtn) {
        skipBtn.addEventListener('click', async () => {
            const sentenceId = window.LessonState.sentences[window.LessonState.currentIndex].id;

            try {
                const response = await fetch('/api/dictation/skip', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ sentenceId: sentenceId })
                });

                if (!response.ok) throw new Error('API error');
                const result = await response.json();
                showCompleted(result, 'feedback-skipped', 'Skipped.');
            } catch (err) {
                console.error('Dictation skip failed:', err);
            }
        });
    }

    updateHeaderNextButtonByPosition();

});
