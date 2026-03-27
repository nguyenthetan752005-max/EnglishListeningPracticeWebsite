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

    // 2. State Listeners
    function handleSentenceChange(index, sentence) {
        // Hiển thị câu mẫu
        if (refTextEl) {
            refTextEl.textContent = sentence.content || '';
        }

        // Reset UI
        if (resultArea) resultArea.textContent = 'Your results will appear here';
        if (nextBtn2) nextBtn2.style.display = 'none';
        if (replayBtn) replayBtn.style.display = 'none';
        if (checkBtn) checkBtn.style.display = '';
        if (skipBtn) skipBtn.style.display = '';

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
            if (checkBtn) checkBtn.style.display = 'none';
            if (skipBtn) skipBtn.style.display = 'none';
            const isLastSentence = window.LessonState.currentIndex >= window.LessonState.sentences.length - 1;
            if (nextBtn2) nextBtn2.style.display = isLastSentence ? 'none' : '';
            if (replayBtn) replayBtn.style.display = '';
        });
    }

    // 4. Proactive Init
    if (window.LessonState && window.LessonState.sentences.length > 0) {
        const curIdx = window.LessonState.currentIndex;
        handleSentenceChange(curIdx, window.LessonState.sentences[curIdx]);
    }
});