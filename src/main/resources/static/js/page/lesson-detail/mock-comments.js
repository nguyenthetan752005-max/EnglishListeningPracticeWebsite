/* [MOCK DATA] DỮ LIỆU BÌNH LUẬN GIẢ.
 * - Dùng để demo giao diện khi chưa có Database.
 * - Cấu trúc: Map với Key là "LessonID_SentenceContent".
 */

window.MockCommentsData = {
    "1_Where is Jane?": [
        {
            userName: "English Learner",
            time: "2 hours ago",
            text: "Tại sao ở đây dùng 'is' mà không phải 'are' vậy mọi người?",
            likes: 5,
            dislikes: 1,
            replies: [
                {
                    userName: "Teacher Minh",
                    time: "1 hour ago",
                    text: "Vì 'Jane' là danh từ số ít (ngôi thứ 3 số ít) nên ta dùng 'is' em nhé.",
                    likes: 12,
                    dislikes: 0
                }
            ]
        },
        {
            userName: "John Doe",
            time: "5 hours ago",
            text: "Câu này đơn giản nhưng rất hay gặp trong giao tiếp hằng ngày.",
            likes: 3,
            dislikes: 0,
            replies: []
        }
    ]
};

window.renderMockComments = function(lessonId, sentenceContent) {
    const listEl = document.getElementById('commentsList');
    const countEl = document.getElementById('commentCount');
    if (!listEl) return;

    const key = `${lessonId}_${sentenceContent}`;
    const comments = window.MockCommentsData[key] || [];

    if (comments.length === 0) {
        listEl.innerHTML = '<p style="text-align: center; color: var(--text-muted); padding: 20px 0;">Chưa có bình luận nào cho câu này.</p>';
        if (countEl) countEl.textContent = "0";
        return;
    }

    if (countEl) countEl.textContent = comments.length;

    const renderCommentHtml = (c, isReply = false) => `
        <div class="comment-item ${isReply ? 'reply' : ''}">
            <div class="comment-avatar ${isReply ? 'small' : ''}">${c.userName.charAt(0)}</div>
            <div class="comment-body">
                <div class="comment-meta">
                    <strong>${c.userName}</strong>
                    <span class="comment-time">${c.time}</span>
                </div>
                <div class="comment-text">${c.text}</div>
                <div class="comment-actions">
                    <button class="comment-action-btn ${isReply ? 'small' : ''}">
                        <i class="fas fa-thumbs-up"></i> ${c.likes || 0}
                    </button>
                    <button class="comment-action-btn ${isReply ? 'small' : ''}">
                        <i class="fas fa-thumbs-down"></i> ${c.dislikes || 0}
                    </button>
                    ${!isReply ? `<button class="comment-action-btn"><i class="fas fa-reply"></i> Reply</button>` : ''}
                </div>
                ${c.replies && c.replies.length > 0 ? `
                    <div class="comment-replies">
                        ${c.replies.map(r => renderCommentHtml(r, true)).join('')}
                    </div>
                ` : ''}
            </div>
        </div>
    `;

    listEl.innerHTML = comments.map(c => renderCommentHtml(c)).join('');
};
