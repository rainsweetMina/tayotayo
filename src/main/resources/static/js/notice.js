
function loadNotices() {
    fetch('https://localhost:8081/ds/api/notices')
        .then(response => {
            console.log("Response status:", response.status);
            if (!response.ok) throw new Error("Network response was not ok");
            return response.json();
        })
        .then(data => {
            console.log("Fetched data:", data);
            const noticeList = document.getElementById('notice-list');
            noticeList.innerHTML = '';
            data.forEach(notice => {
                noticeList.innerHTML += `
                    <div class="notice">
                        <div class="notice-title">${notice.title}</div>
                        <div class="notice-content">${notice.content}</div>
                        <div class="notice-author">작성자: ${notice.author}</div>
                        <button class="button" onclick="deleteNotice(${notice.id})">삭제</button>
                    </div>`;
            });
        })
        .catch(error => console.error("Error fetching notices:", error));
}

function addNotice() {
    const title = document.getElementById('title').value;
    const author = document.getElementById('author').value;
    const content = document.getElementById('content').value;

    fetch('https://localhost:8081/ds/api/notices', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title, author, content })
    }).then(() => {
        loadNotices(); // 또는 location.reload();
    }).catch(error => console.error("Error adding notice:", error));
}

function deleteNotice(id) {
    fetch(`https://localhost:8081/ds/api/notices/${id}`, {
        method: 'DELETE'
    }).then(() => {
        loadNotices(); // 또는 location.reload();
    }).catch(error => console.error("Error deleting notice:", error));
}

loadNotices();
