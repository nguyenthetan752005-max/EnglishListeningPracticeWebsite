/* [MODULE 3: GIAO DIỆN TOÀN VĂN] XỬ LÝ TAB TRANSCRIPT.
 * - Hỗ trợ cả AUDIO và VIDEO lessons
 * - Auto-scroll theo câu đang đọc
 * - Click nút Play nhỏ cạnh từng câu → phát audio hoặc seekTo video
 */

document.addEventListener('DOMContentLoaded', () => {

    const isVideo = (typeof LESSON_TYPE !== 'undefined') && LESSON_TYPE === 'VIDEO';

    const playBtn = document.getElementById('transcriptPlayBtn');
    const timeDisplay = document.getElementById('transcriptTimeDisplay');
    const progressFill = document.getElementById('transcriptProgressFill');
    const progressWrapper = document.getElementById('transcriptProgressWrapper');
    const volumeBtn = document.getElementById('transcriptVolumeBtn');
    const sentenceTextEl = document.getElementById('sentenceText');
    const transcriptSentenceNum = document.getElementById('transcriptSentenceNum');
    const prevBtn = document.getElementById('transcriptPrevBtn');
    const nextBtn = document.getElementById('transcriptNextBtn');
    const transcriptItems = document.querySelectorAll('.transcript-item');
    const repeatCheckbox = document.getElementById('repeatCheckbox');
    const transcriptTab = document.getElementById('transcript-tab');

    function updateTranscriptPlayButtons() {
        transcriptItems.forEach((item) => {
            const playSmallBtn = item.querySelector('.play-small-btn');
            const index = parseInt(item.dataset.index);
            if (!playSmallBtn || isNaN(index)) return;

            const isActivePlaying = index === window.LessonState.currentIndex && window.LessonState.isPlaying;
            playSmallBtn.innerHTML = isActivePlaying ? window.LessonState.pauseSvg : window.LessonState.playSvg;
        });
    }

    function updateTranscriptNextButton() {
        if (!nextBtn) return;
        const isLastSentence = window.LessonState.currentIndex >= window.LessonState.sentences.length - 1;
        nextBtn.style.display = isLastSentence ? 'none' : '';
    }

    function isTranscriptTabActive() {
        return transcriptTab && transcriptTab.classList.contains('active');
    }

    function playCurrentSentence() {
        if (window.LessonState.isVideo) {
            const sentence = window.LessonState.sentences[window.LessonState.currentIndex];
            if (sentence && sentence.startTime != null) {
                document.dispatchEvent(new CustomEvent('video:seekToTime', {
                    detail: { startTime: sentence.startTime, endTime: sentence.endTime }
                }));
            } else {
                window.LessonState.play();
            }
            return;
        }
        window.LessonState.play();
    }

    // --- State Listeners ---
    document.addEventListener('lesson:sentenceChanged', (e) => {
        const sentence = e.detail.sentence;
        
        if (sentenceTextEl) sentenceTextEl.textContent = sentence.content || '';
        if (transcriptSentenceNum) transcriptSentenceNum.textContent = e.detail.index + 1;

        // Reset audio UI (chỉ cho AUDIO mode)
        if (!isVideo) {
            if (progressFill) progressFill.style.width = '0%';
            if (timeDisplay) timeDisplay.textContent = '0:00 / 0:00';
        }

        // Highlight active item
        transcriptItems.forEach(item => item.classList.remove('active'));
        if (transcriptItems[e.detail.index]) {
            transcriptItems[e.detail.index].classList.add('active');
            
            // Auto scroll
            const autoScrollCheckbox = document.getElementById('autoScrollCheckbox');
            if (autoScrollCheckbox && autoScrollCheckbox.checked) {
                transcriptItems[e.detail.index].scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }
        updateTranscriptPlayButtons();
        updateTranscriptNextButton();
    });

    document.addEventListener('lesson:playState', (e) => {
        if (playBtn) {
            playBtn.innerHTML = e.detail.isPlaying ? window.LessonState.pauseSvg : window.LessonState.playSvg;
        }
        updateTranscriptPlayButtons();
    });

    // --- Audio-only UI updates ---
    if (!isVideo) {
        document.addEventListener('lesson:timeUpdate', (e) => {
            if (progressFill) progressFill.style.width = e.detail.percent + '%';
            if (timeDisplay) timeDisplay.textContent = e.detail.timeStr;
        });

        document.addEventListener('lesson:ended', () => {
            if (playBtn) playBtn.innerHTML = window.LessonState.playSvg;

            if (repeatCheckbox && repeatCheckbox.checked) {
                const audio = document.getElementById('mainAudio');
                if (audio) {
                    audio.currentTime = 0;
                    audio.play();
                }
            }
        });

        document.addEventListener('lesson:muteChanged', (e) => {
            if (volumeBtn) {
                const icon = e.detail.muted ? 'fa-volume-mute' : 'fa-volume-up';
                volumeBtn.innerHTML = '<i class="fas ' + icon + '"></i>';
            }
        });
    }

    // Transcript flow: repeat current or auto-play next sentence
    document.addEventListener('lesson:ended', () => {
        if (!isTranscriptTabActive()) return;

        if (playBtn) playBtn.innerHTML = window.LessonState.playSvg;
        updateTranscriptPlayButtons();

        if (repeatCheckbox && repeatCheckbox.checked) {
            playCurrentSentence();
            return;
        }

        const nextIndex = window.LessonState.currentIndex + 1;
        if (nextIndex < window.LessonState.sentences.length) {
            window.LessonState.loadSentence(nextIndex);
            playCurrentSentence();
        }
    });

    // --- UI Event Bindings (Audio mode only) ---
    if (!isVideo) {
        if (playBtn) playBtn.addEventListener('click', () => window.LessonState.togglePlay());
        if (volumeBtn) volumeBtn.addEventListener('click', () => window.LessonState.toggleMute());

        if (progressWrapper) {
            progressWrapper.addEventListener('click', (e) => {
                const rect = progressWrapper.getBoundingClientRect();
                window.LessonState.seekRatio((e.clientX - rect.left) / rect.width);
            });
        }
    }

    // --- Navigation (cả Audio và Video) ---
    if (prevBtn) prevBtn.addEventListener('click', () => { 
        window.LessonState.prev(); 
        window.LessonState.play(); 
    });
    
    if (nextBtn) nextBtn.addEventListener('click', () => { 
        window.LessonState.next(); 
        window.LessonState.play(); 
    });

    // --- List item play buttons ---
    transcriptItems.forEach((item) => {
        const playSmallBtn = item.querySelector('.play-small-btn');
        const index = parseInt(item.dataset.index);

        if (playSmallBtn && !isNaN(index)) {
            playSmallBtn.addEventListener('click', () => {
                if (window.LessonState.currentIndex === index) {
                    window.LessonState.togglePlay();
                    return;
                }

                if (isVideo) {
                    // VIDEO: seekTo video tại startTime
                    window.LessonState.loadSentence(index);
                    playCurrentSentence();
                } else {
                    // AUDIO: load và play audio
                    window.LessonState.loadSentence(index);
                    playCurrentSentence();
                }
            });
        }
    });

    updateTranscriptPlayButtons();
    updateTranscriptNextButton();

});
