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

/* ===========================
   INITIALIZATION
   =========================== */

document.addEventListener('DOMContentLoaded', () => {
    // Initialize result counters for each table
    ['usersTable', 'adminsTable', 'lessonsTable'].forEach(tableId => {
        const table = document.getElementById(tableId);
        if (table) {
            const rows = table.querySelectorAll('tbody tr:not(.empty-row)');
            updateResultCount(tableId, rows.length);
        }
    });
});
