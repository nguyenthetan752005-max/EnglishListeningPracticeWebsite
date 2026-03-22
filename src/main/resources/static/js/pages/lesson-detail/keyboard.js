/* [MODULE 5: PHÍM TẮT] GẮN SỰ KIỆN BÀN PHÍM VÀO TRÌNH DUYỆT.
 * - Phím Space: Play/Pause. Phím Mũi tên: Chuyển câu. Phím Enter: Check đáp án 
 */

/**
 * js/pages/lesson-detail/keyboard.js
 * Centralized keyboard shortcuts for Dictation/Transcript functionality.
 */
document.addEventListener('DOMContentLoaded', () => {

    const inputEl = document.getElementById('dictationInput');
    const checkBtn = document.getElementById('checkBtn');

    document.addEventListener('keydown', (e) => {
        // Prevent triggering shortcuts while typing, EXCEPT for Enter to submit
        if (e.target === inputEl && e.key !== 'Enter') return;

        switch (e.key) {
            case ' ':
                if (e.target !== inputEl) {
                    e.preventDefault(); // Prevent page scroll
                    window.LessonState.togglePlay();
                }
                break;
            case 'ArrowLeft':
                e.preventDefault();
                window.LessonState.prev();
                break;
            case 'ArrowRight':
                e.preventDefault();
                window.LessonState.next();
                break;
            case 'Enter':
                if (e.target === inputEl && checkBtn) {
                    e.preventDefault();
                    checkBtn.click();
                }
                break;
        }
    });

});
