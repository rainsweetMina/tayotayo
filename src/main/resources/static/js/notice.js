let isEditing = false;
let currentEditId = null;

document.getElementById('addNoticeBtn').addEventListener('click', function () {
    const title = document.getElementById('title').value.trim();
    const content = document.getElementById('content').value.trim();
    const author = document.getElementById('author').value.trim();
    const showPopup = document.getElementById('showPopup').checked;
    const popupStart = document.getElementById('popupStart').value;
    const popupEnd = document.getElementById('popupEnd').value;

    if (!title || !content || (!isEditing && !author)) {
        alert('제목, 내용' + (isEditing ? '' : ', 작성자') + '를 입력해 주세요.');
        return;
    }

    const noticeData = {
        title,
        content,
        showPopup,
        popupStart: popupStart || null,
        popupEnd: popupEnd || null
    };

    if (!isEditing) {
        noticeData.author = author;
    }

    const method = isEditing ? 'PUT' : 'POST';
    const url = isEditing ? `/api/admin/notices/${currentEditId}` : '/api/admin/notices';

    fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(noticeData)
    })
        .then(response => {
            if (!response.ok) throw new Error('공지 ' + (isEditing ? '수정' : '등록') + ' 실패');
            return response.status === 204 ? null : response.json();
        })
        .then(() => {
            resetForm();
            loadNotices();
        })
        .catch(err => alert(err.message));
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
                        <div class="notice-popup">
                            팝업 여부: ${notice.showPopup ? '표시' : '미표시'}
                            <br>시작: ${notice.popupStart || '없음'}
                            <br>종료: ${notice.popupEnd || '없음'}
                        </div>
                        <button class="button" onclick="editNotice(${notice.id}, \`${notice.title}\`, \`${notice.content}\`, ${notice.showPopup}, '${notice.popupStart}', '${notice.popupEnd}')">수정</button>
                        <button class="button" onclick="deleteNotice(${notice.id})">삭제</button>
                    </div>
                `;
            });
        })
        .catch(err => console.error(err));
}

function editNotice(id, title, content, showPopup, popupStart, popupEnd) {
    document.getElementById('title').value = title;
    document.getElementById('content').value = content;
    document.getElementById('showPopup').checked = showPopup;
    document.getElementById('popupStart').value = popupStart || '';
    document.getElementById('popupEnd').value = popupEnd || '';

    isEditing = true;
    currentEditId = id;

    document.getElementById('addNoticeBtn').textContent = '수정 완료';
}

function resetForm() {
    document.getElementById('title').value = '';
    document.getElementById('author').value = '';
    document.getElementById('content').value = '';
    document.getElementById('showPopup').checked = false;
    document.getElementById('popupStart').value = '';
    document.getElementById('popupEnd').value = '';

    isEditing = false;
    currentEditId = null;

    document.getElementById('addNoticeBtn').textContent = '공지 추가';
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
