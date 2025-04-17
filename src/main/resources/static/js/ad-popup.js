window.addEventListener('DOMContentLoaded', () => {
    fetch('/api/ad/popup')  // ✅ AdController에서 정의한 URL과 일치
        .then(response => {
            if (!response.ok) throw new Error('광고 팝업 조회 실패');
            return response.json();
        })
        .then(data => {
            if (!data || !data.imageUrl) return; // 보여줄 광고 없으면 종료
            document.getElementById('adImage').src = data.imageUrl;
            document.getElementById('adLink').href = data.linkUrl || '#';
            document.getElementById('adPopup').style.display = 'block';
        })
        .catch(err => console.error(err));
});
