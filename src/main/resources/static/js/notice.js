let isEditing = false;
let currentEditId = null;
let latestNotices = [];

window.addEventListener("error", function (e) {
    console.error("💥 JS Error:", e.message, e.filename, e.lineno);
});


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

    const method = 'POST';
    const url = isEditing
        ? `/api/admin/notices/${currentEditId}`
        : '/api/admin/notices';

    const formData = new FormData();
    const fileInput = document.getElementById("files");

    // ✅ dto 이름 수정: dto → noticeData
    formData.append("notice", new Blob(
        [JSON.stringify(noticeData)],
        { type: "application/json" }
    ));


    if (fileInput && fileInput.files.length > 0) {
        for (let i = 0; i < fileInput.files.length; i++) {
            formData.append("files", fileInput.files[i]);
        }
    }


    // ✅ 수정 시에는 override 헤더 붙여서 PUT처럼 처리
    const headers = isEditing
        ? { 'X-HTTP-Method-Override': 'PUT' }
        : {};

    fetch(url, {
        method,
        headers,
        body: formData
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
        .then(response => response.json())
        .then(data => {
            const noticeList = document.getElementById('noticeList');
            noticeList.innerHTML = ''; // 초기화
            latestNotices = data; // 최신 공지사항 저장

            data.forEach(notice => {
                let filesHtml = '';

                // ✅ 첨부파일이 있을 경우 렌더링
                if (notice.files && notice.files.length > 0) {
                    notice.files.forEach(file => {
                        if (file.fileType && file.fileType.startsWith("image")) {
                            // 이미지일 경우 <img>로 표시
                            filesHtml += `
                                <div style="margin-top: 8px;">
                                    <img src="/files/${file.storedName}" 
                                         alt="${file.originalName}" 
                                         style="max-width: 200px; border: 1px solid #ccc; margin-right: 8px;" />
                                </div>
                            `;
                        } else {
                            // 이미지 외의 파일은 다운로드 링크로
                            filesHtml += `
                                <div style="margin-top: 8px;">
                                    <a href="/files/${file.storedName}" download="${file.originalName}">
                                        📎 ${file.originalName}
                                    </a>
                                </div>
                            `;
                        }
                    });
                }

                // 공지 전체 내용 렌더링
                noticeList.innerHTML += `
                    <div class="notice">
                        <div class="notice-title"><strong>${notice.title}</strong></div>
                        <div class="notice-content">${notice.content}</div>
                        <div class="notice-author">작성자: ${notice.author}</div>
                        <div class="notice-popup">
                            <small>팝업 여부: ${notice.showPopup ? '표시' : '미표시'}</small><br/>
                            <small>시작: ${notice.popupStart || '없음'} / 종료: ${notice.popupEnd || '없음'}</small>
                        </div>
                        <div class="notice-files">
                            ${filesHtml}
                        </div>
                        <div class="notice-actions">
                            <button class="button" onclick="updateNotice(${notice.id})">수정</button>
                            <button class="button" onclick="deleteNotice(${notice.id})">삭제</button>
                        </div>
                        <hr/>
                    </div>
                `;
            });
        })
        .catch(error => {
            console.error('공지사항 로딩 실패:', error);
        });
}

// 멀티 파일 첨부
function handleFileInputChange(input) {
    if (input.files.length > 0) {
        const container = document.getElementById('fileInputs');

        // 이미 마지막 input이면 새 input 생성
        if ([...container.querySelectorAll('input[type="file"]')].every(i => i.files.length > 0)) {
            const newInput = document.createElement('input');
            newInput.type = 'file';
            newInput.name = 'files';
            newInput.className = 'file-input';
            newInput.onchange = function () {
                handleFileInputChange(this);
            };
            container.appendChild(newInput);
        }
    }
}



function updateNotice(id) {
    const notice = latestNotices.find(n => n.id === id);
    if (!notice) {
        alert('공지 데이터를 찾을 수 없습니다.');
        return;
    }

    document.getElementById('title').value = notice.title || '';
    document.getElementById('author').value = notice.author || '';
    document.getElementById('content').value = notice.content || '';
    document.getElementById('showPopup').checked = notice.showPopup || false;
    document.getElementById('popupStart').value = notice.popupStart || '';
    document.getElementById('popupEnd').value = notice.popupEnd || '';

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


    // ✅ 파일 input 모두 제거 후 하나만 남김
    const container = document.getElementById('fileInputs');
    container.innerHTML = ''; // 전체 제거

    const newInput = document.createElement('input');
    newInput.type = 'file';
    newInput.name = 'files';
    newInput.className = 'file-input';
    newInput.onchange = function () {
        handleFileInputChange(this);
    };
    container.appendChild(newInput);

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

