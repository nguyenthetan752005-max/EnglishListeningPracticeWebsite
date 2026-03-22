/* [MODULE 1: TRÁI TIM HỆ THỐNG] QUẢN LÝ BIẾN TOÀN CỤC & AUDIO.
 * - Khởi tạo <audio>, chứa mảng Sentences các câu hỏi 
 * - Quản lý Play, Pause, Chuyển câu 
 * - Dùng event-driven (phát tín hiệu) tới các UI khác thay vì dính chặt code 
 */

/**
 * js/pages/lesson-detail/state.js
 * Core engine managing sentences data and audio playback.
 * Dispatches custom events to decouple UI components.
 */
document.addEventListener('DOMContentLoaded', () => {
    
    // Sort array safely
    const sentencesList = (typeof SENTENCES !== 'undefined') ? SENTENCES : [];
    sentencesList.sort((a, b) => (a.orderIndex || 0) - (b.orderIndex || 0));

    const audio = document.getElementById('mainAudio');
    if (!audio) return;

    window.LessonState = {
        sentences: sentencesList,
        currentIndex: 0,
        isPlaying: false,
        playSvg: '<svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><polygon points="5,3 19,12 5,21"></polygon></svg>',
        pauseSvg: '<svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><rect x="5" y="3" width="4" height="18"></rect><rect x="15" y="3" width="4" height="18"></rect></svg>',

        formatTime: function(seconds) {
            if (isNaN(seconds)) return '0:00';
            const m = Math.floor(seconds / 60);
            const s = Math.floor(seconds % 60);
            return m + ':' + (s < 10 ? '0' : '') + s;
        },

        loadSentence: function(index) {
            if (index < 0 || index >= this.sentences.length) return;
            this.currentIndex = index;
            const sentence = this.sentences[this.currentIndex];

            audio.src = sentence.audioUrl || '';
            audio.load();
            
            // Dispatch event for UI updates
            document.dispatchEvent(new CustomEvent('lesson:sentenceChanged', { 
                detail: { index: this.currentIndex, sentence: sentence } 
            }));
        },

        togglePlay: function() {
            if (!audio.src) return;
            if (audio.paused) {
                audio.play();
            } else {
                audio.pause();
            }
        },

        play: function() { if (audio.src) audio.play(); },
        pause: function() { audio.pause(); },
        
        next: function() { this.loadSentence(this.currentIndex + 1); },
        prev: function() { this.loadSentence(this.currentIndex - 1); },

        setSpeed: function(speed) { audio.playbackRate = parseFloat(speed); },
        toggleMute: function() { 
            audio.muted = !audio.muted;
            document.dispatchEvent(new CustomEvent('lesson:muteChanged', { detail: { muted: audio.muted } }));
        },
        seekRatio: function(ratio) {
            if (audio.duration) audio.currentTime = ratio * audio.duration;
        }
    };

    // --- Audio Event Listeners ---
    audio.addEventListener('play', () => {
        window.LessonState.isPlaying = true;
        document.dispatchEvent(new CustomEvent('lesson:playState', { detail: { isPlaying: true } }));
    });

    audio.addEventListener('pause', () => {
        window.LessonState.isPlaying = false;
        document.dispatchEvent(new CustomEvent('lesson:playState', { detail: { isPlaying: false } }));
    });

    audio.addEventListener('timeupdate', () => {
        if (audio.duration) {
            document.dispatchEvent(new CustomEvent('lesson:timeUpdate', { 
                detail: { 
                    currentTime: audio.currentTime, 
                    duration: audio.duration,
                    percent: (audio.currentTime / audio.duration) * 100,
                    timeStr: window.LessonState.formatTime(audio.currentTime) + ' / ' + window.LessonState.formatTime(audio.duration)
                } 
            }));
        }
    });

    audio.addEventListener('ended', () => {
        document.dispatchEvent(new CustomEvent('lesson:ended'));
    });

    // Init
    if (window.LessonState.sentences.length > 0) {
        window.LessonState.loadSentence(0);
        console.log('Audio State Manager initialized.');
    }
});
