/* ============================================
   ADMIN DASHBOARD - JavaScript
   ============================================ */

/**
 * Switch between sidebar tabs
 */
function switchTab(tabId, element) {
    document.querySelectorAll('.admin-sidebar-item').forEach(el => el.classList.remove('active'));
    element.classList.add('active');
    document.querySelectorAll('.admin-tab-pane').forEach(el => el.classList.remove('active'));
    const target = document.getElementById('tab-' + tabId);
    if (target) target.classList.add('active');
    saveDashboardState({ activeTab: tabId });
}

/* ===========================
   SEARCH & FILTER FUNCTIONS
   =========================== */

/**
 * Filter table rows by text match across all visible columns
 */
function filterTable(tableId, query) {
    const table = document.getElementById(tableId);
    if (!table) return;
    const rows = table.querySelectorAll('tbody tr:not(.empty-row)');
    const q = query.toLowerCase().trim();
    let visibleCount = 0;

    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        const match = !q || text.includes(q);
        row.style.display = match ? '' : 'none';
        if (match) visibleCount++;
    });

    updateResultCount(tableId, visibleCount);
    paginateTable(tableId);
}

/**
 * Filter by data-status attribute (active / inactive)
 */
function filterTableByStatus(tableId, status) {
    const table = document.getElementById(tableId);
    if (!table) return;
    const rows = table.querySelectorAll('tbody tr:not(.empty-row)');
    const searchInput = getSearchInput(tableId);
    const searchQ = searchInput ? searchInput.value.toLowerCase().trim() : '';
    let visibleCount = 0;

    rows.forEach(row => {
        const rowStatus = row.getAttribute('data-status') || '';
        const statusMatch = !status || rowStatus === status;
        const textMatch = !searchQ || row.textContent.toLowerCase().includes(searchQ);
        const visible = statusMatch && textMatch;
        row.style.display = visible ? '' : 'none';
        if (visible) visibleCount++;
    });

    updateResultCount(tableId, visibleCount);
    saveDashboardState({ filterUserStatus: status || 'active' });
    paginateTable(tableId);
}

/**
 * Filter by a specific column's text content (0-indexed)
 */
function filterTableByColumn(tableId, colIndex, value) {
    const table = document.getElementById(tableId);
    if (!table) return;
    const rows = table.querySelectorAll('tbody tr:not(.empty-row)');
    let visibleCount = 0;

    rows.forEach(row => {
        const cells = row.querySelectorAll('td');
        const visible = isRowVisible(cells, colIndex, value, tableId);
        row.style.display = visible ? '' : 'none';
        if (visible) visibleCount++;
    });

    updateResultCount(tableId, visibleCount);
}

/**
 * Check if a row should be visible given all active filters (for lessons)
 */
function isRowVisible(cells, triggeredCol, triggeredVal, tableId) {
    // Text search
    const searchInput = document.getElementById('searchLessons');
    const searchQ = searchInput ? searchInput.value.toLowerCase().trim() : '';
    const row = cells[0] ? cells[0].parentElement : null;
    const textMatch = !searchQ || (row && row.textContent.toLowerCase().includes(searchQ));

    // Type filter (col 4)
    const typeFilter = document.getElementById('filterLessonType');
    const typeVal = (triggeredCol === 4) ? triggeredVal : (typeFilter ? typeFilter.value : '');
    const typeCell = cells[4] ? cells[4].textContent.trim().toUpperCase() : '';
    const typeMatch = !typeVal || typeCell.includes(typeVal.toUpperCase());

    // Level filter (col 2)
    const levelFilter = document.getElementById('filterLessonLevel');
    const levelVal = (triggeredCol === 2) ? triggeredVal : (levelFilter ? levelFilter.value : '');
    const levelCell = cells[2] ? cells[2].textContent.trim().toUpperCase() : '';
    const levelMatch = !levelVal || levelCell.includes(levelVal.toUpperCase());

    return textMatch && typeMatch && levelMatch;
}

/* ===========================
   HELPER FUNCTIONS
   =========================== */

function getSearchInput(tableId) {
    // Derive search input ID from table ID: usersTable -> searchUsers
    const baseName = tableId.replace('Table', '');
    const capitalizedName = baseName.charAt(0).toUpperCase() + baseName.slice(1);
    return document.getElementById('search' + capitalizedName);
}

function updateResultCount(tableId, count) {
    const countEl = document.getElementById(tableId.replace('Table', 'Count'));
    if (countEl) countEl.textContent = count + ' result(s)';
}

function normalizeOptionalString(value) {
    const normalized = String(value ?? '').trim();
    return normalized || null;
}

function getModalElements() {
    return {
        modal: document.getElementById('adminEntityModal'),
        title: document.getElementById('adminEntityModalTitle'),
        subtitle: document.getElementById('adminEntityModalSubtitle'),
        fields: document.getElementById('adminEntityFormFields'),
        error: document.getElementById('adminEntityFormError'),
        submitBtn: document.getElementById('adminEntitySubmitBtn')
    };
}

function escapeAttr(str) {
    return escapeHtml(str).replaceAll('`', '&#096;');
}

function renderTextField(name, label, value = '', required = false, type = 'text', span2 = false, step = '') {
    const requiredAttr = required ? 'required' : '';
    const stepAttr = step ? `step="${step}"` : '';
    return `
        <div class="admin-form-group ${span2 ? 'admin-field-span-2' : ''}">
            <label for="field-${name}">${label}</label>
            <input id="field-${name}" name="${name}" type="${type}" class="admin-form-control" value="${escapeAttr(value)}" ${requiredAttr} ${stepAttr}>
        </div>
    `;
}

function renderHiddenField(name, value = '') {
    return `<input id="field-${name}" name="${name}" type="hidden" value="${escapeAttr(value)}">`;
}

function renderTextareaField(name, label, value = '', required = false, span2 = true) {
    const requiredAttr = required ? 'required' : '';
    return `
        <div class="admin-form-group ${span2 ? 'admin-field-span-2' : ''}">
            <label for="field-${name}">${label}</label>
            <textarea id="field-${name}" name="${name}" class="admin-form-control" ${requiredAttr}>${escapeHtml(value)}</textarea>
        </div>
    `;
}

function renderSelectField(name, label, value, options, span2 = false) {
    const renderedOptions = options.map(option => `
        <option value="${escapeAttr(option.value)}" ${String(option.value) === String(value ?? '') ? 'selected' : ''}>${escapeHtml(option.label)}</option>
    `).join('');
    return `
        <div class="admin-form-group ${span2 ? 'admin-field-span-2' : ''}">
            <label for="field-${name}">${label}</label>
            <select id="field-${name}" name="${name}" class="admin-form-control">${renderedOptions}</select>
        </div>
    `;
}

function renderUploadField(inputId, label, accept, uploadArgs, helpText = '', span2 = true) {
    return `
        <div class="admin-form-group ${span2 ? 'admin-field-span-2' : ''}">
            <label for="${inputId}">${label}</label>
            <input id="${inputId}" type="file" class="admin-form-control" accept="${accept}"
                onchange="uploadAdminAsset('${inputId}', '${uploadArgs.urlField}', '${uploadArgs.publicIdField}', '${uploadArgs.resourceType}', '${uploadArgs.folder}')">
            ${helpText ? `<span class="admin-form-help">${escapeHtml(helpText)}</span>` : ''}
        </div>
    `;
}

function getSelectOptionsFromElement(id) {
    const el = document.getElementById(id);
    if (!el) return [];
    return Array.from(el.options || []).map(option => ({
        value: option.value,
        label: option.textContent || option.value
    }));
}

function showAdminEntityFormError(message) {
    const { error } = getModalElements();
    error.textContent = message || '';
    error.hidden = !message;
}

function setAdminUploadState(isUploading) {
    adminEntityModalState.isUploading = isUploading;
    const { submitBtn } = getModalElements();
    if (submitBtn) {
        submitBtn.disabled = isUploading;
        submitBtn.textContent = isUploading ? 'Uploading...' : (adminEntityModalState.submitLabel || 'Save');
    }
}

function closeAdminEntityModal() {
    const { modal, fields, error } = getModalElements();
    modal.classList.remove('is-open');
    modal.setAttribute('aria-hidden', 'true');
    fields.innerHTML = '';
    error.hidden = true;
    error.textContent = '';
    adminEntityModalState.originalPayload = null;
    adminEntityModalState.isUploading = false;
}

async function sendJson(url, method, payload) {
    const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    const json = await res.json();
    if (!res.ok || !json.success) {
        throw new Error(json.message || 'Server error');
    }
    return json;
}

async function uploadAdminAsset(fileInputId, urlFieldName, publicIdFieldName, resourceType, folder) {
    const fileInput = document.getElementById(fileInputId);
    const file = fileInput?.files?.[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);
    formData.append('resourceType', resourceType || 'auto');
    if (folder) formData.append('folder', folder);

    try {
        setAdminUploadState(true);
        showAdminEntityFormError('');
        const res = await fetch('/api/admin/uploads', {
            method: 'POST',
            body: formData
        });
        const json = await res.json();
        if (!res.ok || !json.success) {
            throw new Error(json.message || 'Upload thất bại.');
        }
        const urlField = document.getElementById('field-' + urlFieldName);
        const publicIdField = document.getElementById('field-' + publicIdFieldName);
        if (urlField) urlField.value = json.data?.url || '';
        if (publicIdField) publicIdField.value = json.data?.publicId || '';
    } catch (e) {
        showAdminEntityFormError(e.message || 'Upload thất bại.');
    } finally {
        setAdminUploadState(false);
        if (fileInput) fileInput.value = '';
    }
}

function invalidateCategoryCaches(categoryId) {
    const categoryKey = String(categoryId ?? '');
    contentCache.sectionsByCategory.delete(categoryKey);
    for (const [sectionId, lessonList] of contentCache.lessonsBySection.entries()) {
        const belongsToCategory = (lessonList || []).some(lesson => String(lesson?.section?.category?.id ?? '') === categoryKey);
        if (belongsToCategory) {
            contentCache.lessonsBySection.delete(sectionId);
        }
    }
}

function invalidateSectionCaches(sectionId) {
    const sectionKey = String(sectionId ?? '');
    const lessons = contentCache.lessonsBySection.get(sectionKey) || [];
    contentCache.lessonsBySection.delete(sectionKey);
    contentCache.sectionsById.delete(sectionKey);
    lessons.forEach(lesson => invalidateLessonCaches(lesson?.id));
}

function invalidateLessonCaches(lessonId) {
    const lessonKey = String(lessonId ?? '');
    contentCache.lessonsById.delete(lessonKey);
    const sentences = contentCache.sentencesByLesson.get(lessonKey) || [];
    contentCache.sentencesByLesson.delete(lessonKey);
    sentences.forEach(sentence => contentCache.sentencesById.delete(String(sentence?.id ?? '')));
}

/* ===========================
   API ACTIONS
   =========================== */

async function deleteUser(id) {
    if (!confirm('Are you sure you want to lock this account?')) return;
    try {
        const res = await fetch('/api/admin/users/' + id, { method: 'DELETE' });
        const json = await res.json();
        if (json.success) location.reload();
        else alert(json.message);
    } catch (e) { alert('Server error'); }
}

async function restoreUser(id) {
    if (!confirm('Restore this account?')) return;
    try {
        const res = await fetch('/api/admin/users/' + id + '/restore', { method: 'POST' });
        const json = await res.json();
        if (json.success) location.reload();
        else alert(json.message);
    } catch (e) { alert('Server error'); }
}

async function hardDeleteSentence(id) {
    if (!confirm('WARNING: This will permanently delete the audio on Cloudinary and the DB record.\nContinue?')) return;
    try {
        const res = await fetch('/api/admin/trash/sentences/' + id, { method: 'DELETE' });
        const json = await res.json();
        if (json.success) location.reload();
        else alert(json.message);
    } catch (e) { alert('Server error'); }
}

async function toggleCommentHide(id) {
    if (!confirm('Ẩn/hiện comment này?')) return;
    try {
        const res = await fetch('/api/admin/comments/' + id + '/toggle-hide', { method: 'PATCH' });
        const json = await res.json();
        if (json.success) { location.reload(); }
        else { alert(json.message || 'Server error'); }
    } catch (e) {
        alert('Server error');
    }
}

async function adminDeleteComment(id) {
    if (!confirm('Xóa (soft delete) comment này?')) return;
    try {
        const res = await fetch('/api/admin/comments/' + id, { method: 'DELETE' });
        const json = await res.json();
        if (json.success) { location.reload(); }
        else { alert(json.message || 'Server error'); }
    } catch (e) {
        alert('Server error');
    }
}

async function toggleSlideshowActive(id) {
    try {
        const res = await fetch('/api/admin/slideshows/' + id + '/toggle-active', { method: 'PATCH' });
        const json = await res.json();
        if (json.success) { location.reload(); }
        else { alert(json.message || 'Server error'); }
    } catch (e) {
        alert('Server error');
    }
}

async function deleteSlideshow(id) {
    if (!confirm('Xóa slideshow này?')) return;
    try {
        const res = await fetch('/api/admin/slideshows/' + id, { method: 'DELETE' });
        const json = await res.json();
        if (json.success) { location.reload(); }
        else { alert(json.message || 'Server error'); }
    } catch (e) {
        alert('Server error');
    }
}

function getCategoryRow(id) {
    return document.querySelector(`#categoriesTable tbody tr[data-id="${id}"]`);
}

function openAdminEntityModal(config) {
    const { modal, title, subtitle, fields, error, submitBtn } = getModalElements();
    adminEntityModalState.entityType = config.entityType;
    adminEntityModalState.mode = config.mode;
    adminEntityModalState.entityId = config.entityId ?? null;
    adminEntityModalState.submitUrl = config.submitUrl;
    adminEntityModalState.submitMethod = config.submitMethod;
    adminEntityModalState.afterSubmit = config.afterSubmit;
    adminEntityModalState.originalPayload = config.initialPayload ? normalizePayloadForCompare(config.entityType, config.initialPayload) : null;
    adminEntityModalState.submitLabel = config.submitLabel || 'Save';

    title.textContent = config.title;
    subtitle.textContent = config.subtitle;
    fields.innerHTML = config.fieldsHtml;
    error.hidden = true;
    error.textContent = '';
    submitBtn.textContent = adminEntityModalState.submitLabel;
    submitBtn.disabled = false;
    modal.classList.add('is-open');
    modal.setAttribute('aria-hidden', 'false');
}

function buildCategoryFormFields(data = {}) {
    return [
        renderTextField('name', 'Category name', data.name || '', true),
        renderTextField('levelRange', 'Level range', data.levelRange || ''),
        renderSelectField('practiceType', 'Practice type', data.practiceType || 'LISTENING', [
            { value: 'LISTENING', label: 'LISTENING' },
            { value: 'SPEAKING', label: 'SPEAKING' }
        ]),
        renderSelectField('type', 'Media type', data.type || 'AUDIO', [
            { value: 'AUDIO', label: 'AUDIO' },
            { value: 'VIDEO', label: 'VIDEO' }
        ]),
        renderSelectField('status', 'Status', data.status || 'DRAFT', [
            { value: 'DRAFT', label: 'DRAFT' },
            { value: 'PUBLISHED', label: 'PUBLISHED' },
            { value: 'ARCHIVED', label: 'ARCHIVED' }
        ]),
        renderTextField('orderIndex', 'Order index', data.orderIndex ?? 0, true, 'number'),
        renderHiddenField('cloudImageId', data.cloudImageId || ''),
        renderTextField('imageUrl', 'Image URL', data.imageUrl || '', false, 'text', true),
        renderUploadField('categoryImageUpload', 'Upload image', 'image/*', {
            urlField: 'imageUrl',
            publicIdField: 'cloudImageId',
            resourceType: 'image',
            folder: 'categories'
        }, 'Sau khi upload, link ảnh sẽ tự điền vào ô Image URL.'),
        renderTextareaField('description', 'Description', data.description || '')
    ].join('');
}

function buildSectionFormFields(data = {}) {
    return [
        renderSelectField('categoryId', 'Category', data.categoryId || getSelectValue('sectionsCategoryFilter'), getSelectOptionsFromElement('sectionsCategoryFilter')),
        renderTextField('name', 'Section name', data.name || '', true),
        renderSelectField('status', 'Status', data.status || 'DRAFT', [
            { value: 'DRAFT', label: 'DRAFT' },
            { value: 'PUBLISHED', label: 'PUBLISHED' },
            { value: 'ARCHIVED', label: 'ARCHIVED' }
        ]),
        renderTextField('orderIndex', 'Order index', data.orderIndex ?? 0, true, 'number'),
        renderTextareaField('description', 'Description', data.description || '')
    ].join('');
}

function buildLessonFormFields(data = {}) {
    return [
        renderSelectField('sectionId', 'Section', data.sectionId || getSelectValue('lessonsSectionFilter'), getSelectOptionsFromElement('lessonsSectionFilter')),
        renderTextField('title', 'Lesson title', data.title || '', true),
        renderTextField('level', 'Level', data.level || ''),
        renderTextField('youtubeVideoId', 'YouTube link or ID', data.youtubeVideoId || '', false, 'text', true),
        renderSelectField('status', 'Status', data.status || 'DRAFT', [
            { value: 'DRAFT', label: 'DRAFT' },
            { value: 'PUBLISHED', label: 'PUBLISHED' },
            { value: 'ARCHIVED', label: 'ARCHIVED' }
        ]),
        renderTextField('orderIndex', 'Order index', data.orderIndex ?? 0, true, 'number')
    ].join('');
}

function buildSentenceFormFields(data = {}) {
    return [
        renderSelectField('lessonId', 'Lesson', data.lessonId || getSelectValue('sentencesLessonFilter'), getSelectOptionsFromElement('sentencesLessonFilter'), true),
        renderTextareaField('content', 'Sentence content', data.content || '', true),
        renderHiddenField('cloudAudioId', data.cloudAudioId || ''),
        renderTextField('audioUrl', 'Audio URL', data.audioUrl || '', false, 'text', true),
        renderUploadField('sentenceAudioUpload', 'Upload audio', 'audio/*', {
            urlField: 'audioUrl',
            publicIdField: 'cloudAudioId',
            resourceType: 'auto',
            folder: 'sentences'
        }, 'Sau khi upload, link audio sẽ tự điền vào ô Audio URL.'),
        renderTextField('startTime', 'Start time (seconds)', data.startTime ?? '', false, 'number', false, '0.01'),
        renderTextField('endTime', 'End time (seconds)', data.endTime ?? '', false, 'number', false, '0.01'),
        renderSelectField('status', 'Status', data.status || 'DRAFT', [
            { value: 'DRAFT', label: 'DRAFT' },
            { value: 'PUBLISHED', label: 'PUBLISHED' },
            { value: 'ARCHIVED', label: 'ARCHIVED' }
        ]),
        renderTextField('orderIndex', 'Order index', data.orderIndex ?? 0, true, 'number')
    ].join('');
}

function collectAdminEntityPayload(entityType) {
    const formData = new FormData(document.getElementById('adminEntityForm'));
    const raw = Object.fromEntries(formData.entries());

    if (entityType === 'category') {
        return {
            name: String(raw.name || '').trim(),
            imageUrl: normalizeOptionalString(raw.imageUrl),
            cloudImageId: normalizeOptionalString(raw.cloudImageId),
            levelRange: normalizeOptionalString(raw.levelRange),
            type: raw.type,
            practiceType: raw.practiceType,
            description: normalizeOptionalString(raw.description),
            status: raw.status,
            orderIndex: Number(raw.orderIndex || 0)
        };
    }

    if (entityType === 'section') {
        return {
            categoryId: Number(raw.categoryId),
            name: String(raw.name || '').trim(),
            description: normalizeOptionalString(raw.description),
            status: raw.status,
            orderIndex: Number(raw.orderIndex || 0)
        };
    }

    if (entityType === 'lesson') {
        return {
            sectionId: Number(raw.sectionId),
            title: String(raw.title || '').trim(),
            youtubeVideoId: normalizeOptionalString(raw.youtubeVideoId),
            level: normalizeOptionalString(raw.level),
            status: raw.status,
            orderIndex: Number(raw.orderIndex || 0)
        };
    }

    return {
        lessonId: Number(raw.lessonId),
        content: String(raw.content || '').trim(),
        audioUrl: normalizeOptionalString(raw.audioUrl),
        cloudAudioId: normalizeOptionalString(raw.cloudAudioId),
        startTime: raw.startTime === '' ? null : Number(raw.startTime),
        endTime: raw.endTime === '' ? null : Number(raw.endTime),
        orderIndex: Number(raw.orderIndex || 0),
        status: raw.status
    };
}

function validateAdminEntityPayload(entityType, payload) {
    if (entityType === 'category' && !payload.name) return 'Tên danh mục không được để trống.';
    if (entityType === 'section' && !payload.categoryId) return 'Danh mục không được để trống.';
    if (entityType === 'section' && !payload.name) return 'Tên section không được để trống.';
    if (entityType === 'lesson' && !payload.sectionId) return 'Section không được để trống.';
    if (entityType === 'lesson' && !payload.title) return 'Tiêu đề bài học không được để trống.';
    if (entityType === 'sentence' && !payload.lessonId) return 'Lesson không được để trống.';
    if (entityType === 'sentence' && !payload.content) return 'Nội dung sentence không được để trống.';
    if (Number.isNaN(payload.orderIndex)) return 'Order index không hợp lệ.';
    if (entityType === 'sentence') {
        if (payload.startTime != null && Number.isNaN(payload.startTime)) return 'Start time không hợp lệ.';
        if (payload.endTime != null && Number.isNaN(payload.endTime)) return 'End time không hợp lệ.';
    }
    if (entityType === 'lesson') {
        const section = contentCache.sectionsById.get(String(payload.sectionId));
        const lessonType = String(section?.category?.type || 'AUDIO').toUpperCase();
        const youtubeValue = normalizeOptionalString(payload.youtubeVideoId);
        if (lessonType === 'VIDEO') {
            if (!youtubeValue) return 'Link YouTube không được để trống với bài học video.';
            if (!extractYoutubeVideoId(youtubeValue)) return 'Link YouTube không hợp lệ.';
            payload.youtubeVideoId = extractYoutubeVideoId(youtubeValue);
        }
    }
    return null;
}

function extractYoutubeVideoId(value) {
    const normalized = String(value || '').trim();
    if (!normalized) return null;
    if (/^[A-Za-z0-9_-]{11}$/.test(normalized)) return normalized;
    const match = normalized.match(/(?:youtube\.com\/watch\?v=|youtu\.be\/|youtube\.com\/embed\/)([A-Za-z0-9_-]{11})/i);
    return match ? match[1] : null;
}

function normalizePayloadForCompare(entityType, payload) {
    if (!payload) return null;
    if (entityType === 'category') {
        return {
            name: String(payload.name || '').trim(),
            imageUrl: normalizeOptionalString(payload.imageUrl),
            cloudImageId: normalizeOptionalString(payload.cloudImageId),
            levelRange: normalizeOptionalString(payload.levelRange),
            type: payload.type || 'AUDIO',
            practiceType: payload.practiceType || 'LISTENING',
            description: normalizeOptionalString(payload.description),
            status: payload.status || 'DRAFT',
            orderIndex: Number(payload.orderIndex || 0)
        };
    }
    if (entityType === 'section') {
        return {
            categoryId: Number(payload.categoryId),
            name: String(payload.name || '').trim(),
            description: normalizeOptionalString(payload.description),
            status: payload.status || 'DRAFT',
            orderIndex: Number(payload.orderIndex || 0)
        };
    }
    if (entityType === 'lesson') {
        return {
            sectionId: Number(payload.sectionId),
            title: String(payload.title || '').trim(),
            youtubeVideoId: normalizeOptionalString(extractYoutubeVideoId(payload.youtubeVideoId) || payload.youtubeVideoId),
            level: normalizeOptionalString(payload.level),
            status: payload.status || 'DRAFT',
            orderIndex: Number(payload.orderIndex || 0)
        };
    }
    return {
        lessonId: Number(payload.lessonId),
        content: String(payload.content || '').trim(),
        audioUrl: normalizeOptionalString(payload.audioUrl),
        cloudAudioId: normalizeOptionalString(payload.cloudAudioId),
        startTime: payload.startTime === '' ? null : (payload.startTime == null ? null : Number(payload.startTime)),
        endTime: payload.endTime === '' ? null : (payload.endTime == null ? null : Number(payload.endTime)),
        orderIndex: Number(payload.orderIndex || 0),
        status: payload.status || 'DRAFT'
    };
}

function payloadEquals(left, right) {
    return JSON.stringify(left) === JSON.stringify(right);
}

async function submitAdminEntityForm(event) {
    event.preventDefault();
    if (adminEntityModalState.isUploading) {
        showAdminEntityFormError('Vui lòng chờ upload hoàn tất.');
        return;
    }
    const payload = collectAdminEntityPayload(adminEntityModalState.entityType);
    const validationError = validateAdminEntityPayload(adminEntityModalState.entityType, payload);
    if (validationError) {
        showAdminEntityFormError(validationError);
        return;
    }
    const normalizedPayload = normalizePayloadForCompare(adminEntityModalState.entityType, payload);
    if (adminEntityModalState.mode === 'edit' && payloadEquals(normalizedPayload, adminEntityModalState.originalPayload)) {
        showAdminEntityFormError('Dữ liệu chưa thay đổi.');
        return;
    }

    try {
        const response = await sendJson(adminEntityModalState.submitUrl, adminEntityModalState.submitMethod, payload);
        closeAdminEntityModal();
        alert(response.message || 'Thao tác thành công.');
        if (typeof adminEntityModalState.afterSubmit === 'function') {
            await adminEntityModalState.afterSubmit(payload);
        }
    } catch (e) {
        showAdminEntityFormError(e.message || 'Server error');
    }
}

async function createCategory() {
    const initialPayload = {
        name: '',
        imageUrl: null,
        cloudImageId: null,
        levelRange: null,
        type: 'AUDIO',
        practiceType: 'LISTENING',
        description: null,
        status: 'DRAFT',
        orderIndex: 0
    };
    openAdminEntityModal({
        entityType: 'category',
        mode: 'create',
        submitUrl: '/api/admin/categories',
        submitMethod: 'POST',
        title: 'Create Category',
        subtitle: 'Enter the editable properties for a new category.',
        submitLabel: 'Create',
        fieldsHtml: buildCategoryFormFields(initialPayload),
        initialPayload,
        afterSubmit: async () => location.reload()
    });
}

async function editCategory(id) {
    const row = getCategoryRow(id);
    if (!row) return;
    openAdminEntityModal({
        entityType: 'category',
        mode: 'edit',
        entityId: id,
        submitUrl: '/api/admin/categories/' + id,
        submitMethod: 'PUT',
        title: 'Edit Category',
        subtitle: 'Update only the editable properties.',
        submitLabel: 'Save changes',
        fieldsHtml: buildCategoryFormFields({
            name: row.dataset.name || '',
            imageUrl: row.dataset.imageUrl || '',
            cloudImageId: row.dataset.cloudImageId || '',
            levelRange: row.dataset.levelRange || '',
            description: row.dataset.description || '',
            status: row.dataset.status || 'DRAFT',
            orderIndex: row.dataset.orderIndex || 0,
            practiceType: row.dataset.categoryPractice || 'LISTENING',
            type: row.dataset.categoryType || 'AUDIO'
        }),
        initialPayload: {
            name: row.dataset.name || '',
            imageUrl: row.dataset.imageUrl || '',
            cloudImageId: row.dataset.cloudImageId || '',
            levelRange: row.dataset.levelRange || '',
            description: row.dataset.description || '',
            status: row.dataset.status || 'DRAFT',
            orderIndex: row.dataset.orderIndex || 0,
            practiceType: row.dataset.categoryPractice || 'LISTENING',
            type: row.dataset.categoryType || 'AUDIO'
        },
        afterSubmit: async () => location.reload()
    });
}

async function deleteCategory(id) {
    if (!confirm('Xóa mềm Category này? Section bên dưới phải trống.')) return;
    try {
        const res = await fetch('/api/admin/categories/' + id, { method: 'DELETE' });
        const json = await res.json();
        if (json.success) location.reload();
        else alert(json.message);
    } catch (e) {
        alert('Server error');
    }
}

async function createSection() {
    const categoryId = Number(getSelectValue('sectionsCategoryFilter'));
    if (!categoryId) {
        alert('Please choose a category first.');
        return;
    }
    openAdminEntityModal({
        entityType: 'section',
        mode: 'create',
        submitUrl: '/api/admin/sections',
        submitMethod: 'POST',
        title: 'Create Section',
        subtitle: 'Choose category and fill section properties.',
        submitLabel: 'Create',
        fieldsHtml: buildSectionFormFields({ categoryId }),
        initialPayload: {
            categoryId,
            name: '',
            description: null,
            status: 'DRAFT',
            orderIndex: 0
        },
        afterSubmit: async (payload) => {
            invalidateCategoryCaches(payload.categoryId);
            await onSectionsCategoryChange();
            await onLessonsCategoryChange();
            await onSentencesCategoryChange();
        }
    });
}

async function editSection(id) {
    const section = contentCache.sectionsById.get(String(id));
    if (!section) return;
    openAdminEntityModal({
        entityType: 'section',
        mode: 'edit',
        entityId: id,
        submitUrl: '/api/admin/sections/' + id,
        submitMethod: 'PUT',
        title: 'Edit Section',
        subtitle: 'Update the editable properties of this section.',
        submitLabel: 'Save changes',
        fieldsHtml: buildSectionFormFields({
            categoryId: section?.category?.id,
            name: section.name || '',
            description: section.description || '',
            status: section.status || 'DRAFT',
            orderIndex: section.orderIndex ?? 0
        }),
        initialPayload: {
            categoryId: section?.category?.id,
            name: section.name || '',
            description: section.description || '',
            status: section.status || 'DRAFT',
            orderIndex: section.orderIndex ?? 0
        },
        afterSubmit: async (payload) => {
            invalidateCategoryCaches(payload.categoryId);
            await onSectionsCategoryChange();
            await onLessonsCategoryChange();
            await onSentencesCategoryChange();
        }
    });
}

async function deleteSection(id) {
    if (!confirm('Xóa mềm Section này? Lesson bên dưới phải trống.')) return;
    try {
        const res = await fetch('/api/admin/sections/' + id, { method: 'DELETE' });
        const json = await res.json();
        if (json.success) {
            invalidateSectionCaches(id);
            await onSectionsCategoryChange();
            await onLessonsCategoryChange();
            await onSentencesCategoryChange();
        } else {
            alert(json.message);
        }
    } catch (e) {
        alert('Server error');
    }
}

async function createLesson() {
    const sectionId = Number(getSelectValue('lessonsSectionFilter'));
    if (!sectionId) {
        alert('Please choose a section first.');
        return;
    }
    openAdminEntityModal({
        entityType: 'lesson',
        mode: 'create',
        submitUrl: '/api/admin/lessons',
        submitMethod: 'POST',
        title: 'Create Lesson',
        subtitle: 'Fill the lesson properties. Auto-calculated fields are omitted.',
        submitLabel: 'Create',
        fieldsHtml: buildLessonFormFields({ sectionId }),
        initialPayload: {
            sectionId,
            title: '',
            youtubeVideoId: null,
            level: null,
            status: 'DRAFT',
            orderIndex: 0
        },
        afterSubmit: async (payload) => {
            invalidateSectionCaches(payload.sectionId);
            await onLessonsCategoryChange();
            await onSentencesCategoryChange();
        }
    });
}

async function editLesson(id) {
    const lesson = contentCache.lessonsById.get(String(id));
    if (!lesson) return;
    openAdminEntityModal({
        entityType: 'lesson',
        mode: 'edit',
        entityId: id,
        submitUrl: '/api/admin/lessons/' + id,
        submitMethod: 'PUT',
        title: 'Edit Lesson',
        subtitle: 'Update the lesson properties shown below.',
        submitLabel: 'Save changes',
        fieldsHtml: buildLessonFormFields({
            sectionId: lesson?.section?.id,
            title: lesson.title || '',
            level: lesson.level || '',
            youtubeVideoId: lesson.youtubeVideoId || '',
            status: lesson.status || 'DRAFT',
            orderIndex: lesson.orderIndex ?? 0
        }),
        initialPayload: {
            sectionId: lesson?.section?.id,
            title: lesson.title || '',
            level: lesson.level || '',
            youtubeVideoId: lesson.youtubeVideoId || '',
            status: lesson.status || 'DRAFT',
            orderIndex: lesson.orderIndex ?? 0
        },
        afterSubmit: async (payload) => {
            invalidateSectionCaches(payload.sectionId);
            await onLessonsCategoryChange();
            await onSentencesCategoryChange();
        }
    });
}

async function deleteLesson(id) {
    if (!confirm('Xóa mềm Lesson này? Sentence bên dưới phải trống.')) return;
    try {
        const res = await fetch('/api/admin/lessons/' + id, { method: 'DELETE' });
        const json = await res.json();
        if (json.success) {
            invalidateLessonCaches(id);
            await onLessonsCategoryChange();
            await onSentencesCategoryChange();
        } else {
            alert(json.message);
        }
    } catch (e) {
        alert('Server error');
    }
}

async function createSentence() {
    const lessonId = Number(getSelectValue('sentencesLessonFilter'));
    if (!lessonId) {
        alert('Please choose a lesson first.');
        return;
    }
    openAdminEntityModal({
        entityType: 'sentence',
        mode: 'create',
        submitUrl: '/api/admin/sentences',
        submitMethod: 'POST',
        title: 'Create Sentence',
        subtitle: 'Enter the sentence content and media timing.',
        submitLabel: 'Create',
        fieldsHtml: buildSentenceFormFields({ lessonId }),
        initialPayload: {
            lessonId,
            content: '',
            audioUrl: null,
            cloudAudioId: null,
            startTime: null,
            endTime: null,
            status: 'DRAFT',
            orderIndex: 0
        },
        afterSubmit: async (payload) => {
            invalidateLessonCaches(payload.lessonId);
            await renderSentencesTable();
            await onLessonsCategoryChange();
        }
    });
}

async function editSentence(id) {
    const sentence = contentCache.sentencesById.get(String(id));
    if (!sentence) return;
    openAdminEntityModal({
        entityType: 'sentence',
        mode: 'edit',
        entityId: id,
        submitUrl: '/api/admin/sentences/' + id,
        submitMethod: 'PUT',
        title: 'Edit Sentence',
        subtitle: 'Update only the editable fields of this sentence.',
        submitLabel: 'Save changes',
        fieldsHtml: buildSentenceFormFields({
            lessonId: sentence?.lesson?.id,
            content: sentence.content || '',
            audioUrl: sentence.audioUrl || '',
            cloudAudioId: sentence.cloudAudioId || '',
            startTime: sentence.startTime ?? '',
            endTime: sentence.endTime ?? '',
            status: sentence.status || 'DRAFT',
            orderIndex: sentence.orderIndex ?? 0
        }),
        initialPayload: {
            lessonId: sentence?.lesson?.id,
            content: sentence.content || '',
            audioUrl: sentence.audioUrl || '',
            cloudAudioId: sentence.cloudAudioId || '',
            startTime: sentence.startTime ?? '',
            endTime: sentence.endTime ?? '',
            status: sentence.status || 'DRAFT',
            orderIndex: sentence.orderIndex ?? 0
        },
        afterSubmit: async (payload) => {
            invalidateLessonCaches(payload.lessonId);
            await renderSentencesTable();
            await onLessonsCategoryChange();
        }
    });
}

async function deleteSentence(id) {
    if (!confirm('Xóa mềm Sentence này?')) return;
    try {
        const sentence = contentCache.sentencesById.get(String(id));
        const res = await fetch('/api/admin/sentences/' + id, { method: 'DELETE' });
        const json = await res.json();
        if (json.success) {
            invalidateLessonCaches(sentence?.lesson?.id || getSelectValue('sentencesLessonFilter'));
            await renderSentencesTable();
            await onLessonsCategoryChange();
        } else {
            alert(json.message);
        }
    } catch (e) {
        alert('Server error');
    }
}

function testMediaSource(url) {
    if (!url) {
        alert('Media URL is missing');
        return;
    }
    window.open(url, '_blank', 'noopener,noreferrer');
}

/* ===========================
   CONTENT MANAGEMENT TABS
   =========================== */

const contentCache = {
    categoriesById: new Map(),
    sectionsByCategory: new Map(),
    sectionsById: new Map(),
    lessonsBySection: new Map(),
    lessonsById: new Map(),
    sentencesByLesson: new Map(),
    sentencesById: new Map()
};

const DASHBOARD_STATE_KEY = 'adminDashboardFiltersV1';
const sentencesPagination = {
    allRows: [],
    page: 1,
    pageSize: 20,
    totalPages: 1
};
const tablePagerState = {};
const adminEntityModalState = {
    entityType: null,
    mode: 'create',
    entityId: null,
    submitUrl: '',
    submitMethod: 'POST',
    afterSubmit: null
};

function readDashboardState() {
    try {
        return JSON.parse(localStorage.getItem(DASHBOARD_STATE_KEY) || '{}');
    } catch (e) {
        return {};
    }
}

function saveDashboardState(patch = {}) {
    const nextState = { ...readDashboardState(), ...patch };
    localStorage.setItem(DASHBOARD_STATE_KEY, JSON.stringify(nextState));
}

function shouldForceOverviewTab() {
    return document.body?.dataset?.forceOverview === 'true';
}

function setElementValue(id, value) {
    const el = document.getElementById(id);
    if (!el) return;
    el.value = value ?? '';
}

function getLessonType(lesson) {
    return String(lesson?.section?.category?.type || 'AUDIO').toUpperCase();
}

function escapeHtml(str) {
    return String(str ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function abbreviateText(str, maxLen) {
    const s = String(str ?? '');
    const clean = s.replace(/\s+/g, ' ').trim();
    if (!clean) return '';
    return clean.length > maxLen ? clean.slice(0, maxLen) + '...' : clean;
}

function escapeJsString(str) {
    return String(str ?? '')
        .replaceAll('\\', '\\\\')
        .replaceAll("'", "\\'");
}

function setEmptyRow(tableId, message, colspan) {
    const table = document.getElementById(tableId);
    if (!table) return;
    const tbody = table.querySelector('tbody');
    if (!tbody) return;
    tbody.innerHTML = `
        <tr class="empty-row">
            <td colspan="${colspan}">${escapeHtml(message)}</td>
        </tr>
    `;
}

function filterRenderedTable(tableId, query) {
    const table = document.getElementById(tableId);
    if (!table) return;
    const rows = table.querySelectorAll('tbody tr:not(.empty-row)');
    const q = (query || '').toLowerCase().trim();
    let visibleCount = 0;

    rows.forEach(row => {
        const visible = !q || row.textContent.toLowerCase().includes(q);
        row.style.display = visible ? '' : 'none';
        if (visible) visibleCount++;
    });

    updateResultCount(tableId, visibleCount);
    if (tableId === 'sectionsTable') {
        saveDashboardState({ searchSections: query || '' });
    }
    paginateTable(tableId);
}

function getSelectValue(id) {
    const el = document.getElementById(id);
    return el ? el.value : '';
}

function setSelectOptions(id, options, placeholder, includeAll = false) {
    const select = document.getElementById(id);
    if (!select) return;
    if (!options || options.length === 0) {
        select.innerHTML = '';
        return;
    }
    select.innerHTML = options.map(o => `<option value="${o.id}">${escapeHtml(o.name || o.title || ('#' + o.id))}</option>`).join('');
}

async function fetchSectionsByCategory(categoryId) {
    if (!categoryId) return [];
    const key = String(categoryId);
    if (contentCache.sectionsByCategory.has(key)) return contentCache.sectionsByCategory.get(key);
    const res = await fetch('/api/admin/content/categories/' + categoryId + '/sections');
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const sections = await res.json();
    (sections || []).forEach(section => contentCache.sectionsById.set(String(section.id), section));
    contentCache.sectionsByCategory.set(key, sections || []);
    return sections || [];
}

async function fetchLessonsBySection(sectionId) {
    if (!sectionId) return [];
    const key = String(sectionId);
    if (contentCache.lessonsBySection.has(key)) return contentCache.lessonsBySection.get(key);
    const res = await fetch('/api/admin/content/sections/' + sectionId + '/lessons');
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const lessons = await res.json();
    (lessons || []).forEach(l => contentCache.lessonsById.set(String(l.id), l));
    contentCache.lessonsBySection.set(key, lessons || []);
    return lessons || [];
}

async function fetchSentencesByLesson(lessonId) {
    if (!lessonId) return [];
    const key = String(lessonId);
    if (contentCache.sentencesByLesson.has(key)) return contentCache.sentencesByLesson.get(key);
    const res = await fetch('/api/admin/content/lessons/' + lessonId + '/sentences');
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const sentences = await res.json();
    (sentences || []).forEach(sentence => contentCache.sentencesById.set(String(sentence.id), sentence));
    contentCache.sentencesByLesson.set(key, sentences || []);
    return sentences || [];
}

function filterCategoriesByType(typeVal) {
    const table = document.getElementById('categoriesTable');
    if (!table) return;
    const searchQ = (document.getElementById('searchCategories')?.value || '').toLowerCase().trim();
    const practiceVal = (document.getElementById('filterCategoryPractice')?.value || '').toUpperCase();
    const levelVal = (document.getElementById('filterCategoryLevel')?.value || '').toUpperCase();
    let visibleCount = 0;
    table.querySelectorAll('tbody tr:not(.empty-row)').forEach(row => {
        const rowType = (row.getAttribute('data-category-type') || '').toUpperCase();
        const rowPractice = (row.getAttribute('data-category-practice') || '').toUpperCase();
        const rowLevels = (row.getAttribute('data-category-levels') || '').toUpperCase().split(',').filter(Boolean);
        const typeMatch = rowType === String(typeVal || '').toUpperCase();
        const practiceMatch = rowPractice === practiceVal;
        const levelMatch = !levelVal || rowLevels.includes(levelVal);
        const textMatch = !searchQ || row.textContent.toLowerCase().includes(searchQ);
        const visible = typeMatch && practiceMatch && levelMatch && textMatch;
        row.style.display = visible ? '' : 'none';
        if (visible) visibleCount++;
    });
    updateResultCount('categoriesTable', visibleCount);
    saveDashboardState({
        searchCategories: document.getElementById('searchCategories')?.value || '',
        filterCategoryType: typeVal || '',
        filterCategoryPractice: document.getElementById('filterCategoryPractice')?.value || '',
        filterCategoryLevel: document.getElementById('filterCategoryLevel')?.value || ''
    });
    paginateTable('categoriesTable');
}

async function onSectionsCategoryChange() {
    const categoryId = getSelectValue('sectionsCategoryFilter');
    saveDashboardState({ sectionsCategoryFilter: categoryId });
    if (!categoryId) {
        setEmptyRow('sectionsTable', 'Select a Category to load Sections', 3);
        updateResultCount('sectionsTable', 0);
        return;
    }

    setEmptyRow('sectionsTable', 'Loading sections...', 3);
    try {
        const sections = await fetchSectionsByCategory(categoryId);
        if (!sections.length) {
            setEmptyRow('sectionsTable', 'No sections found', 3);
            updateResultCount('sectionsTable', 0);
            return;
        }
        const tbody = document.querySelector('#sectionsTable tbody');
        if (!tbody) return;
        tbody.innerHTML = sections.map(s => `
            <tr data-id="${s.id}">
                <td><strong>${escapeHtml(s.name)}</strong></td>
                <td>${s.category?.name ? escapeHtml(s.category.name) : 'N/A'}</td>
                <td>
                    <div class="admin-action-group">
                        <button class="admin-btn admin-btn-outline admin-btn-small" onclick="editSection(${s.id})"><i class="fas fa-pen"></i></button>
                        <button class="admin-btn admin-btn-danger admin-btn-small" onclick="deleteSection(${s.id})"><i class="fas fa-trash"></i></button>
                    </div>
                </td>
            </tr>
        `).join('');
        filterRenderedTable('sectionsTable', document.getElementById('searchSections')?.value || '');
        paginateTable('sectionsTable');
    } catch (e) {
        setEmptyRow('sectionsTable', 'Failed to load sections', 3);
    }
}

async function onLessonsCategoryChange() {
    const categoryId = getSelectValue('lessonsCategoryFilter');
    saveDashboardState({ lessonsCategoryFilter: categoryId, lessonsSectionFilter: '' });
    if (!categoryId) {
        setSelectOptions('lessonsSectionFilter', [], '', false);
        setEmptyRow('lessonsTable', 'Select a Category to load Lessons', 5);
        updateResultCount('lessonsTable', 0);
        return;
    }

    try {
        const sections = await fetchSectionsByCategory(categoryId);
        setSelectOptions('lessonsSectionFilter', sections, '', false);
        await renderLessonsTable();
    } catch (e) {
        setEmptyRow('lessonsTable', 'Failed to load lessons', 5);
        updateResultCount('lessonsTable', 0);
    }
}

async function gatherLessonsForCurrentFilter() {
    const categoryId = getSelectValue('lessonsCategoryFilter');
    const sectionId = getSelectValue('lessonsSectionFilter');
    if (!categoryId) return [];

    const sections = await fetchSectionsByCategory(categoryId);
    const selectedSections = sectionId ? sections.filter(s => String(s.id) === String(sectionId)) : sections;
    const lessonGroups = await Promise.all(selectedSections.map(s => fetchLessonsBySection(String(s.id))));
    return lessonGroups.flat();
}

async function renderLessonsTable() {
    const categoryId = getSelectValue('lessonsCategoryFilter');
    if (!categoryId) return;

    const sectionFilter = getSelectValue('lessonsSectionFilter');
    const levelFilter = (getSelectValue('filterLessonLevel') || '').toUpperCase();
    const searchQ = (document.getElementById('searchLessons')?.value || '').toLowerCase().trim();
    saveDashboardState({
        lessonsSectionFilter: sectionFilter,
        searchLessons: document.getElementById('searchLessons')?.value || '',
        filterLessonLevel: document.getElementById('filterLessonLevel')?.value || ''
    });
    setEmptyRow('lessonsTable', 'Loading lessons...', 5);
    try {
        const lessons = await gatherLessonsForCurrentFilter();
        syncLessonLevelOptions(lessons);
        const filtered = lessons.filter(l => {
            const textMatch = !searchQ || JSON.stringify(l).toLowerCase().includes(searchQ);
            const levelMatch = !levelFilter || String(l.level || '').trim().toUpperCase() === levelFilter;
            return textMatch && levelMatch;
        });
        if (!filtered.length) {
            setEmptyRow('lessonsTable', 'No lessons found', 5);
            updateResultCount('lessonsTable', 0);
            return;
        }
        const tbody = document.querySelector('#lessonsTable tbody');
        if (!tbody) return;
        tbody.innerHTML = filtered.map(l => `
            <tr data-id="${l.id}">
                <td><strong>${escapeHtml(l.title)}</strong></td>
                <td><span class="admin-badge admin-level-badge">${escapeHtml(l.level ?? '')}</span></td>
                <td><span class="admin-badge ${getLessonType(l) === 'VIDEO' ? 'badge-admin' : 'badge-user'}">${escapeHtml(getLessonType(l))}</span></td>
                <td>${l.section?.name ? escapeHtml(l.section.name) : 'N/A'}</td>
                <td>
                    <div class="admin-action-group">
                        <button class="admin-btn admin-btn-outline admin-btn-small" onclick="editLesson(${l.id})"><i class="fas fa-pen"></i></button>
                        <button class="admin-btn admin-btn-danger admin-btn-small" onclick="deleteLesson(${l.id})"><i class="fas fa-trash"></i></button>
                    </div>
                </td>
            </tr>
        `).join('');
        updateResultCount('lessonsTable', filtered.length);
        paginateTable('lessonsTable');
    } catch (e) {
        setEmptyRow('lessonsTable', 'Failed to load lessons', 5);
        updateResultCount('lessonsTable', 0);
    }
}

function syncLessonLevelOptions(lessons) {
    const select = document.getElementById('filterLessonLevel');
    if (!select) return;
    const previousValue = select.value || '';
    const levels = Array.from(new Set((lessons || [])
        .map(lesson => String(lesson?.level || '').trim())
        .filter(Boolean)))
        .sort((a, b) => a.localeCompare(b));
    select.innerHTML = `<option value="">All Levels</option>${levels.map(level => `<option value="${escapeHtml(level)}">${escapeHtml(level)}</option>`).join('')}`;
    select.value = levels.includes(previousValue) ? previousValue : '';
}

async function onSentencesCategoryChange() {
    const categoryId = getSelectValue('sentencesCategoryFilter');
    saveDashboardState({ sentencesCategoryFilter: categoryId, sentencesSectionFilter: '', sentencesLessonFilter: '' });
    if (!categoryId) {
        setSelectOptions('sentencesSectionFilter', [], '', false);
        setSelectOptions('sentencesLessonFilter', [], '', false);
        setEmptyRow('sentencesTable', 'Select a Category to load Sentences', 7);
        updateResultCount('sentencesTable', 0);
        return;
    }

    try {
        const sections = await fetchSectionsByCategory(categoryId);
        setSelectOptions('sentencesSectionFilter', sections, '', false);
        await refreshLessonsOptionsForSentences();
        await renderSentencesTable();
    } catch (e) {
        setEmptyRow('sentencesTable', 'Failed to load sentences', 7);
        updateResultCount('sentencesTable', 0);
    }
}

async function onSentencesSectionChange() {
    saveDashboardState({ sentencesSectionFilter: getSelectValue('sentencesSectionFilter'), sentencesLessonFilter: '' });
    await refreshLessonsOptionsForSentences();
    await renderSentencesTable();
}

async function refreshLessonsOptionsForSentences() {
    const categoryId = getSelectValue('sentencesCategoryFilter');
    const sectionId = getSelectValue('sentencesSectionFilter');
    if (!categoryId) {
        setSelectOptions('sentencesLessonFilter', [], '', false);
        return;
    }

    const sections = await fetchSectionsByCategory(categoryId);
    const selectedSections = sectionId ? sections.filter(s => String(s.id) === String(sectionId)) : sections;
    const lessonGroups = await Promise.all(selectedSections.map(s => fetchLessonsBySection(String(s.id))));
    const lessons = lessonGroups.flat();
    setSelectOptions('sentencesLessonFilter', lessons, '', false);
}

function buildSentenceMediaSource(sentence, lesson) {
    const lessonType = getLessonType(lesson);
    if (lessonType === 'VIDEO') {
        const videoId = (lesson?.youtubeVideoId || '').trim();
        return videoId ? ('YouTube ID: ' + videoId) : 'Missing YouTube video ID';
    }
    const audio = (sentence?.audioUrl || '').trim();
    return audio || 'Missing sentence audio URL';
}

function buildSentenceMediaUrl(sentence, lesson) {
    const lessonType = getLessonType(lesson);
    if (lessonType === 'VIDEO') {
        const videoId = (lesson?.youtubeVideoId || '').trim();
        const startSeconds = Math.max(0, Math.floor(Number(sentence?.startTime || 0)));
        return videoId ? (`https://www.youtube.com/watch?v=${videoId}&start=${startSeconds}`) : '';
    }
    return (sentence?.audioUrl || '').trim();
}

async function renderSentencesTable() {
    const categoryId = getSelectValue('sentencesCategoryFilter');
    if (!categoryId) return;

    const sectionId = getSelectValue('sentencesSectionFilter');
    const lessonId = getSelectValue('sentencesLessonFilter');
    const searchRaw = document.getElementById('searchSentences')?.value || '';
    const searchQ = searchRaw.toLowerCase().trim();

    sentencesPagination.page = 1;
    saveDashboardState({
        sentencesSectionFilter: sectionId,
        sentencesLessonFilter: lessonId,
        searchSentences: searchRaw,
        sentencesPageSize: String(sentencesPagination.pageSize)
    });

    if (!sectionId) {
        setEmptyRow('sentencesTable', 'Choose a Section to load Sentences', 7);
        sentencesPagination.allRows = [];
        sentencesPagination.totalPages = 1;
        updateResultCount('sentencesTable', 0);
        updateSentencesPaginationUi();
        return;
    }

    setEmptyRow('sentencesTable', 'Loading sentences...', 7);
    try {
        const sections = await fetchSectionsByCategory(categoryId);
        const selectedSections = sectionId ? sections.filter(s => String(s.id) === String(sectionId)) : sections;
        const lessonGroups = await Promise.all(selectedSections.map(s => fetchLessonsBySection(String(s.id))));
        let lessons = lessonGroups.flat();
        if (lessonId) lessons = lessons.filter(l => String(l.id) === String(lessonId));

        const sentenceGroups = await Promise.all(lessons.map(async (l) => {
            const sentences = await fetchSentencesByLesson(String(l.id));
            return sentences.map(st => ({ sentence: st, lesson: l }));
        }));

        const rows = sentenceGroups.flat().filter(item => {
            if (!searchQ) return true;
            return (item.sentence?.content || '').toLowerCase().includes(searchQ)
                || (item.lesson?.title || '').toLowerCase().includes(searchQ)
                || buildSentenceMediaSource(item.sentence, item.lesson).toLowerCase().includes(searchQ);
        });

        if (!rows.length) {
            setEmptyRow('sentencesTable', 'No sentences found', 7);
            sentencesPagination.allRows = [];
            sentencesPagination.totalPages = 1;
            updateResultCount('sentencesTable', 0);
            updateSentencesPaginationUi();
            return;
        }

        sentencesPagination.allRows = rows;
        sentencesPagination.totalPages = Math.max(1, Math.ceil(rows.length / sentencesPagination.pageSize));
        renderSentencesPage();
        updateResultCount('sentencesTable', rows.length);
    } catch (e) {
        setEmptyRow('sentencesTable', 'Failed to load sentences', 7);
        sentencesPagination.allRows = [];
        sentencesPagination.totalPages = 1;
        updateResultCount('sentencesTable', 0);
        updateSentencesPaginationUi();
    }
}

function renderSentencesPage() {
    const tbody = document.querySelector('#sentencesTable tbody');
    if (!tbody) return;

    const total = sentencesPagination.allRows.length;
    if (!total) {
        setEmptyRow('sentencesTable', 'No sentences found', 7);
        updateSentencesPaginationUi();
        return;
    }

    if (sentencesPagination.page > sentencesPagination.totalPages) {
        sentencesPagination.page = sentencesPagination.totalPages;
    }
    if (sentencesPagination.page < 1) {
        sentencesPagination.page = 1;
    }

    const start = (sentencesPagination.page - 1) * sentencesPagination.pageSize;
    const end = start + sentencesPagination.pageSize;
    const pageRows = sentencesPagination.allRows.slice(start, end);

    tbody.innerHTML = pageRows.map(item => {
        const mediaType = getLessonType(item.lesson);
        const mediaSource = buildSentenceMediaSource(item.sentence, item.lesson);
        const mediaUrl = buildSentenceMediaUrl(item.sentence, item.lesson);
        const checkCell = mediaUrl
            ? `<button class="admin-btn admin-btn-outline admin-btn-small" onclick="testMediaSource('${escapeJsString(mediaUrl)}')"><i class="fas fa-play"></i> Test</button>`
            : '<span class="admin-badge badge-inactive">MISSING</span>';
        return `
            <tr data-id="${item.sentence?.id ?? ''}">
                <td>${escapeHtml(item.sentence?.orderIndex ?? '')}</td>
                <td>${escapeHtml(abbreviateText(item.sentence?.content ?? '', 90))}</td>
                <td>${escapeHtml(item.lesson?.title ?? 'N/A')}</td>
                <td><span class="admin-badge ${mediaType === 'VIDEO' ? 'badge-admin' : 'badge-user'}">${mediaType}</span></td>
                <td>${escapeHtml(abbreviateText(mediaSource, 60))}</td>
                <td>${checkCell}</td>
                <td>
                    <div class="admin-action-group">
                        <button class="admin-btn admin-btn-outline admin-btn-small" onclick="editSentence(${item.sentence?.id})"><i class="fas fa-pen"></i></button>
                        <button class="admin-btn admin-btn-danger admin-btn-small" onclick="deleteSentence(${item.sentence?.id})"><i class="fas fa-trash"></i></button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    saveDashboardState({ sentencesPage: String(sentencesPagination.page) });
    updateSentencesPaginationUi();
}

function updateSentencesPaginationUi() {
    const pageInfo = document.getElementById('sentencesPageInfo');
    const prevBtn = document.getElementById('sentencesPrevBtn');
    const nextBtn = document.getElementById('sentencesNextBtn');
    if (pageInfo) pageInfo.textContent = `Page ${sentencesPagination.page} / ${sentencesPagination.totalPages}`;
    if (prevBtn) prevBtn.disabled = sentencesPagination.page <= 1;
    if (nextBtn) nextBtn.disabled = sentencesPagination.page >= sentencesPagination.totalPages;
}

function goSentencesPage(delta) {
    if (!sentencesPagination.allRows.length) return;
    sentencesPagination.page += delta;
    renderSentencesPage();
}

function onSentencesPageSizeChange() {
    const size = Number(getSelectValue('sentencesPageSize'));
    sentencesPagination.pageSize = Number.isFinite(size) && size > 0 ? size : 20;
    sentencesPagination.page = 1;
    saveDashboardState({ sentencesPageSize: String(sentencesPagination.pageSize), sentencesPage: '1' });
    void renderSentencesTable();
}

function openUserProfile(userId) {
    window.location.href = '/admin/users/' + userId + '/profile';
}

function sortUsersByTopScore(mode) {
    const tbody = document.querySelector('#usersTable tbody');
    if (!tbody) return;
    const rows = Array.from(tbody.querySelectorAll('tr:not(.empty-row)'));
    rows.sort((a, b) => {
        const scoreA = Number(a.getAttribute('data-top-score') || 0);
        const scoreB = Number(b.getAttribute('data-top-score') || 0);
        return mode === 'low' ? scoreA - scoreB : scoreB - scoreA;
    });
    rows.forEach(row => tbody.appendChild(row));
    saveDashboardState({ filterUserTopScore: mode || 'high' });
    paginateTable('usersTable');
}

function ensureTablePager(tableId) {
    const table = document.getElementById(tableId);
    if (!table || document.getElementById(tableId + 'Pager')) return;
    const wrapper = table.closest('.admin-table-wrapper');
    if (!wrapper) return;
    const pager = document.createElement('div');
    pager.id = tableId + 'Pager';
    pager.className = 'admin-filter-bar';
    pager.style.justifyContent = 'flex-end';
    pager.style.padding = '10px 12px';
    pager.innerHTML = `
        <button class="admin-btn admin-btn-outline admin-btn-small" onclick="goTablePage('${tableId}', -1)">Prev</button>
        <span class="admin-result-count" id="${tableId}PagerInfo">Page 1 / 1</span>
        <button class="admin-btn admin-btn-outline admin-btn-small" onclick="goTablePage('${tableId}', 1)">Next</button>
    `;
    wrapper.insertAdjacentElement('afterend', pager);
}

function paginateTable(tableId) {
    const table = document.getElementById(tableId);
    if (!table) return;
    ensureTablePager(tableId);
    if (!tablePagerState[tableId]) tablePagerState[tableId] = { page: 1, pageSize: 12 };
    const state = tablePagerState[tableId];
    const rows = Array.from(table.querySelectorAll('tbody tr:not(.empty-row)'));
    const visibleRows = rows.filter(row => row.style.display !== 'none');
    const totalPages = Math.max(1, Math.ceil(visibleRows.length / state.pageSize));
    if (state.page > totalPages) state.page = totalPages;
    if (state.page < 1) state.page = 1;
    const start = (state.page - 1) * state.pageSize;
    const end = start + state.pageSize;
    visibleRows.forEach((row, index) => {
        row.style.display = (index >= start && index < end) ? '' : 'none';
    });
    const info = document.getElementById(tableId + 'PagerInfo');
    if (info) info.textContent = `Page ${state.page} / ${totalPages}`;
}

function goTablePage(tableId, delta) {
    if (!tablePagerState[tableId]) tablePagerState[tableId] = { page: 1, pageSize: 12 };
    tablePagerState[tableId].page += delta;
    paginateTable(tableId);
}

async function restoreDashboardState() {
    const state = readDashboardState();

    setElementValue('searchCategories', state.searchCategories || '');
    setElementValue('filterCategoryPractice', state.filterCategoryPractice || 'LISTENING');
    setElementValue('filterCategoryType', state.filterCategoryType || 'AUDIO');
    setElementValue('filterCategoryLevel', state.filterCategoryLevel || '');
    filterCategoriesByType(getSelectValue('filterCategoryType'));

    setElementValue('sectionsCategoryFilter', state.sectionsCategoryFilter || document.getElementById('sectionsCategoryFilter')?.options?.[0]?.value || '');
    await onSectionsCategoryChange();
    setElementValue('searchSections', state.searchSections || '');
    filterRenderedTable('sectionsTable', state.searchSections || '');

    setElementValue('lessonsCategoryFilter', state.lessonsCategoryFilter || document.getElementById('lessonsCategoryFilter')?.options?.[0]?.value || '');
    await onLessonsCategoryChange();
    setElementValue('lessonsSectionFilter', state.lessonsSectionFilter || getSelectValue('lessonsSectionFilter'));
    setElementValue('filterLessonLevel', state.filterLessonLevel || '');
    setElementValue('searchLessons', state.searchLessons || '');
    await renderLessonsTable();

    setElementValue('sentencesPageSize', state.sentencesPageSize || '20');
    sentencesPagination.pageSize = Number(getSelectValue('sentencesPageSize')) || 20;
    setElementValue('sentencesCategoryFilter', state.sentencesCategoryFilter || document.getElementById('sentencesCategoryFilter')?.options?.[0]?.value || '');
    await onSentencesCategoryChange();
    setElementValue('sentencesSectionFilter', state.sentencesSectionFilter || getSelectValue('sentencesSectionFilter'));
    await onSentencesSectionChange();
    setElementValue('sentencesLessonFilter', state.sentencesLessonFilter || getSelectValue('sentencesLessonFilter'));
    setElementValue('searchSentences', state.searchSentences || '');
    await renderSentencesTable();
    const savedPage = Number(state.sentencesPage || '1');
    sentencesPagination.page = Number.isFinite(savedPage) && savedPage > 0 ? savedPage : 1;
    renderSentencesPage();

    setElementValue('filterUserStatus', state.filterUserStatus || 'active');
    filterTableByStatus('usersTable', getSelectValue('filterUserStatus'));
    setElementValue('filterUserTopScore', state.filterUserTopScore || 'high');
    sortUsersByTopScore(getSelectValue('filterUserTopScore'));

    const activeTab = shouldForceOverviewTab() ? 'overview' : (state.activeTab || 'overview');
    if (shouldForceOverviewTab()) {
        saveDashboardState({ activeTab: 'overview' });
    }
    const sidebarItem = document.querySelector(`.admin-sidebar-item[onclick="switchTab('${activeTab}', this)"]`);
    if (sidebarItem) switchTab(activeTab, sidebarItem);
}

/* ===========================
   INITIALIZATION
   =========================== */

document.addEventListener('DOMContentLoaded', async () => {
    document.querySelectorAll('#categoriesTable tbody tr[data-id]').forEach(row => {
        contentCache.categoriesById.set(String(row.dataset.id), {
            id: Number(row.dataset.id),
            name: row.dataset.name || '',
            imageUrl: row.dataset.imageUrl || '',
            cloudImageId: row.dataset.cloudImageId || '',
            levelRange: row.dataset.levelRange || '',
            description: row.dataset.description || '',
            status: row.dataset.status || 'DRAFT',
            orderIndex: Number(row.dataset.orderIndex || 0),
            practiceType: row.dataset.categoryPractice || 'LISTENING',
            type: row.dataset.categoryType || 'AUDIO'
        });
    });
    // Initialize result counters for each table
    ['usersTable', 'adminsTable', 'categoriesTable'].forEach(tableId => {
        const table = document.getElementById(tableId);
        if (table) {
            const rows = table.querySelectorAll('tbody tr:not(.empty-row)');
            updateResultCount(tableId, rows.length);
        }
    });
    ['usersTable', 'adminsTable', 'categoriesTable', 'sectionsTable', 'lessonsTable', 'trashUsersTable', 'trashSentencesTable'].forEach(tableId => paginateTable(tableId));
    await restoreDashboardState();
});
