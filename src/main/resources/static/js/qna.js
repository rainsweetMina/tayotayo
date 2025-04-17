// ✅ 답변 등록 함수
function answerQna(id) {
    const answer = prompt("답변을 입력하세요:");
    if (!answer) return;

    fetch(`/api/qna/${id}/answer`, {  // ✅ 경로 수정
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ answer })
    }).then(() => loadQnAs());
}

// ✅ 전체 QnA 목록 불러오기
function loadQnAs() {
    fetch("/api/qna/admin")  // ✅ 경로 수정
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById("qnaList");
            tbody.innerHTML = "";

            data.forEach(q => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${q.id}</td>
                    <td>${q.title}</td>
                    <td>${q.content}</td>
                    <td>${q.memberId}</td>
                    <td>${q.createdAt?.slice(0, 10)}</td>
                    <td>${q.answer || ""}</td>
                    <td><button onclick="answerQna(${q.id})">답변</button></td>
                `;
                tbody.appendChild(row);
            });
        });
}

// ✅ 페이지 로드 시 자동 실행
document.addEventListener("DOMContentLoaded", () => {
    loadQnAs();
});
