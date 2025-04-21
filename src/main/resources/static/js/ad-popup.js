document.addEventListener('DOMContentLoaded', () => {
    fetch('/api/ad/popup')
        .then(res => res.json())
        .then(data => {
            if (!data || !data.imageUrl) return;

            const imagePath = data.imageUrl.startsWith('http')
                ? data.imageUrl
                : `/uploads/ad/${data.imageUrl}`;

            document.getElementById('adImage').src = imagePath;
            document.getElementById('adLink').href = data.linkUrl || '#';
            document.getElementById('adPopup').style.display = 'block';
        })
        .catch(err => console.error('광고 팝업 오류:', err));
});
