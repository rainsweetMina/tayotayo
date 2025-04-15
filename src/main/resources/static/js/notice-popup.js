window.addEventListener('DOMContentLoaded', () => {
    fetch('/notice/popup')  // ✅ URL 수정
        .then(response => {
            if (!response.ok) throw new Error('팝업 데이터 조회 실패');
            return response.json();
        })
        .then(data => {
            if (!data || !data.title) return; // 보여줄 팝업 없으면 종료
            document.getElementById('popupTitle').textContent = data.title;
            document.getElementById('popupContent').textContent = data.content;
            document.getElementById('noticePopup').style.display = 'block';
        })
        .catch(err => console.error(err));
});
