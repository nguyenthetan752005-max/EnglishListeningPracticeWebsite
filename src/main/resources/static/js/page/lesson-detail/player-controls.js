/* [MODULE: PLAYER CONTROLS] QUẢN LÝ CÁC NÚT ĐIỀU KHIỂN CHUNG.
 * - Quản lý nút Play (cả Audio bar và Video panel).
 * - Quản lý Navigation (Prev/Next/Counter).
 * - Quản lý âm lượng, tốc độ, thanh tiến trình.
 */

document.addEventListener('DOMContentLoaded', () => {
    
    // 1. Elements
    const counterEl = document.getElementById('currentSentenceNum');
    const totalEl = document.getElementById('totalSentences');
    
    const playBtnAudio = document.getElementById('dictPlayBtn');
    const playBtnVideo = document.getElementById('videoDictPlayBtn');

    const timeDisplay = document.getElementById('dictTimeDisplay');
    const progressFill = document.getElementById('dictProgressFill');
    const progressWrapper = document.getElementById('dictProgressWrapper');
    
    const volumeBtn = document.getElementById('dictVolumeBtn');
    const speedSelect = document.getElementById('dictSpeedSelect');
    
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    
    // 2. State Listeners
    function updateNavArrows(index) {
        if (!nextBtn) return;
        const isLastSentence = index >= window.LessonState.sentences.length - 1;
        nextBtn.style.display = isLastSentence ? 'none' : '';
    }

    document.addEventListener('lesson:sentenceChanged', (e) => {
        if (counterEl) counterEl.textContent = e.detail.index + 1;
        if (totalEl) totalEl.textContent = window.LessonState.sentences.length;
        
        // Reset Progress UI
        if (progressFill) progressFill.style.width = '0%';
        if (timeDisplay) timeDisplay.textContent = '0:00 / 0:00';
        
        updateNavArrows(e.detail.index);
    });

    document.addEventListener('lesson:playState', (e) => {
        const icon = e.detail.isPlaying ? window.LessonState.pauseSvg : window.LessonState.playSvg;
        if (playBtnAudio) playBtnAudio.innerHTML = icon;
        if (playBtnVideo) playBtnVideo.innerHTML = icon;
    });

    document.addEventListener('lesson:timeUpdate', (e) => {
        if (progressFill) progressFill.style.width = e.detail.percent + '%';
        if (timeDisplay) timeDisplay.textContent = e.detail.timeStr;
    });

    document.addEventListener('lesson:ended', () => {
        if (playBtnAudio) playBtnAudio.innerHTML = window.LessonState.playSvg;
        if (playBtnVideo) playBtnVideo.innerHTML = window.LessonState.playSvg;
    });

    document.addEventListener('lesson:muteChanged', (e) => {
        if (volumeBtn) {
            const icon = e.detail.muted ? 'fa-volume-mute' : 'fa-volume-up';
            volumeBtn.innerHTML = '<i class="fas ' + icon + '"></i>';
        }
    });

    // 3. UI Event Bindings
    const togglePlay = () => {
        window.LessonState.togglePlay();
    };
    if (playBtnAudio) playBtnAudio.addEventListener('click', togglePlay);
    if (playBtnVideo) playBtnVideo.addEventListener('click', togglePlay);
    
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

    // 4. Proactive Init (Khắc phục Race Condition)
    if (window.LessonState && window.LessonState.sentences.length > 0) {
        const idx = window.LessonState.currentIndex;
        if (counterEl) counterEl.textContent = idx + 1;
        if (totalEl) totalEl.textContent = window.LessonState.sentences.length;
        updateNavArrows(idx);
    }
});