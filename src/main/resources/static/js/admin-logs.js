let currentPage = 0;
const pageSize = 10;

document.addEventListener('DOMContentLoaded', () => {
    loadLogs(currentPage);
});

function loadLogs(page) {
    fetch(`/api/admin/logs?page=${page}&size=${pageSize}`)
        .then(res => res.json())
        .then(data => {
            renderTable(data.content);
            renderPagination(data.totalPages, data.number);
        });
}

function renderTable(logs) {
    const tbody = document.querySelector('#logTable tbody');
    tbody.innerHTML = '';

    logs.forEach(log => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${log.timestamp.replace('T', ' ')}</td>
            <td>${log.adminId}</td>
            <td>${log.action}</td>
            <td>${log.target}</td>
            <td><pre>${log.beforeValue ?? '-'}</pre></td>
            <td><pre>${log.afterValue ?? '-'}</pre></td>
        `;
        tbody.appendChild(tr);
    });
}

function renderPagination(totalPages, currentPage) {
    const container = document.getElementById('pagination');
    container.innerHTML = '';

    for (let i = 0; i < totalPages; i++) {
        const btn = document.createElement('button');
        btn.textContent = i + 1;
        if (i === currentPage) btn.disabled = true;
        btn.onclick = () => loadLogs(i);
        container.appendChild(btn);
    }
}
