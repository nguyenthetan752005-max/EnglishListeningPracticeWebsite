/* [MODULE 3: GIAO DIỆN TOÀN VĂN] XỬ LÝ TAB TRANSCRIPT.
 * - Tạo hiệu ứng cuộn trang (Auto-scroll) theo câu đang đọc 
 * - Quản lý việc ấn vào nút Play nhỏ cạnh từng câu văn 
 */

/**
 * js/pages/lesson-detail/transcript-ui.js
 * Handles the logic specifically for the Transcript Tab (split layout).
 */
document.addEventListener('DOMContentLoaded', () => {

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

    // --- State Listeners ---
    document.addEventListener('lesson:sentenceChanged', (e) => {
        const sentence = e.detail.sentence;
        
        if (sentenceTextEl) sentenceTextEl.textContent = sentence.content || '';
        if (transcriptSentenceNum) transcriptSentenceNum.textContent = e.detail.index + 1;
        if (progressFill) progressFill.style.width = '0%';
        if (timeDisplay) timeDisplay.textContent = '0:00 / 0:00';

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

        if (repeatCheckbox && repeatCheckbox.checked) {
            // Restart audio if repeat is on
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

    // --- UI Event Bindings ---
    if (playBtn) playBtn.addEventListener('click', () => window.LessonState.togglePlay());
    
    if (prevBtn) prevBtn.addEventListener('click', () => { 
        window.LessonState.prev(); 
        window.LessonState.play(); 
    });
    
    if (nextBtn) nextBtn.addEventListener('click', () => { 
        window.LessonState.next(); 
        window.LessonState.play(); 
    });
    
    if (volumeBtn) volumeBtn.addEventListener('click', () => window.LessonState.toggleMute());

    if (progressWrapper) {
        progressWrapper.addEventListener('click', (e) => {
            const rect = progressWrapper.getBoundingClientRect();
            window.LessonState.seekRatio((e.clientX - rect.left) / rect.width);
        });
    }

    // List item play buttons
    transcriptItems.forEach((item) => {
        const playSmallBtn = item.querySelector('.play-small-btn');
        const index = parseInt(item.dataset.index);

        if (playSmallBtn && !isNaN(index)) {
            playSmallBtn.addEventListener('click', () => {
                window.LessonState.loadSentence(index);
                window.LessonState.play();
            });
        }
    });

});
