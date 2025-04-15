let isEditing = false;
let currentEditId = null;

document.getElementById('addNoticeBtn').addEventListener('click', function () {
    const title = document.getElementById('title').value.trim();
    const content = document.getElementById('content').value.trim();
    const author = document.getElementById('author').value.trim();

    if (isEditing) {
        // 수정 요청 - author는 필요 없음
        if (!title || !content) {
            alert('제목과 내용을 입력해 주세요.');
            return;
        }

        const noticeData = { title, content }; // author 제외

        fetch(`/api/admin/notices/${currentEditId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(noticeData)
        })
            .then(response => {
                if (!response.ok) throw new Error('공지 수정 실패');
                return response.status === 204 ? null : response.json();
            })
            .then(() => {
                resetForm();
                loadNotices();
            })
            .catch(err => alert(err.message));
    } else {
        // 등록 요청 - author 포함
        if (!title || !author || !content) {
            alert('제목, 작성자, 내용을 모두 입력해 주세요.');
            return;
        }

        const noticeData = { title, author, content };

        fetch('/api/admin/notices', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(noticeData)
        })
            .then(response => {
                if (!response.ok) throw new Error('공지 추가 실패');
                return response.status === 204 ? null : response.json();
            })
            .then(() => {
                resetForm();
                loadNotices();
            })
            .catch(err => alert(err.message));
    }
});

function loadNotices() {
    fetch('/api/admin/notices')
        .then(response => {
            if (!response.ok) throw new Error('공지 불러오기 실패');
            return response.json();
        })
        .then(data => {
            const noticeList = document.getElementById('notice-list');
            noticeList.innerHTML = '';
            data.forEach(notice => {
                noticeList.innerHTML += `
                    <div class="notice">
                        <div class="notice-title">${notice.title}</div>
                        <div class="notice-content">${notice.content}</div>
                        <div class="notice-author">작성자: ${notice.author}</div>
                        <button class="button" onclick="editNotice(${notice.id}, '${notice.title}', '${notice.content}')">수정</button>
                        <button class="button" onclick="deleteNotice(${notice.id})">삭제</button>
                    </div>
                `;
            });
        })
        .catch(err => console.error(err));
}

function editNotice(id, title, content) {
    document.getElementById('title').value = title;
    document.getElementById('content').value = content;

    isEditing = true;
    currentEditId = id;

    const btn = document.getElementById('addNoticeBtn');
    btn.textContent = '수정 완료';
}

function resetForm() {
    document.getElementById('title').value = '';
    document.getElementById('author').value = '';
    document.getElementById('content').value = '';

    isEditing = false;
    currentEditId = null;

    const btn = document.getElementById('addNoticeBtn');
    btn.textContent = '공지 추가';
}

function deleteNotice(id) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    fetch(`/api/admin/notices/${id}`, {
        method: 'DELETE'
    })
        .then(response => {
            if (!response.ok) throw new Error('공지 삭제 실패');
            return response.status === 204 ? null : response.json();
        })
        .then(() => loadNotices())
        .catch(err => alert(err.message));
}

loadNotices();
