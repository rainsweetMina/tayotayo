// 오늘 날짜를 불러오기
const today = new Date().toISOString().split('T')[0];
const runDate = document.getElementById("runDate");
runDate.min = today;    // 최소값을 오늘로 지정 (과거 날짜 선택 방지)
runDate.value = today;  // 기본값을 오늘로 지정

// 버튼 및 테이블 document 요소 가져오기
const editBtn = document.getElementById("editBtn");
const saveBtn = document.getElementById("saveBtn");
const table = document.getElementById("schedule-table");
const tbody = document.getElementById("schedule-tbody");

// DB 노선 (route_no) 가져오기
document.getElementById("routeNo").addEventListener("change", () => {
    const routeNo = document.getElementById("routeNo").value;

    fetch(`/api/route-notes?routeNo=${routeNo}`)
        .then(res => res.json())
        .then(data => {
            const routeNoteSelect = document.getElementById("routeNote");
            routeNoteSelect.innerHTML = "";

            data.forEach(note => {
                const option = document.createElement("option");
                option.value = note;
                option.textContent = note;
                routeNoteSelect.appendChild(option);
            });
        });
});

// 노선, 방면에 맞는 데이터 구하기
document.getElementById("routeNote").addEventListener("change", () => {
    const routeNo = document.getElementById("routeNo").value;
    const routeNote = document.getElementById("routeNote").value;
    if (!routeNo || !routeNote) return;

    fetch(`/api/schedules?routeNo=${routeNo}&routeNote=${routeNote}`)
        .then(res => res.json())
        .then(renderScheduleTable);
});
// 수정 버튼 클릭 이벤트
editBtn.addEventListener("click", () => {
    table.querySelectorAll("td[contenteditable]").forEach(td => {
        td.setAttribute("contenteditable", "true");
        td.style.backgroundColor = "#fff8dc"; // 편집 시 표시
    });
    // 수정 버튼 클릭시 저장버튼으로 체인지
    editBtn.style.display = "none";
    saveBtn.style.display = "inline-block";
});

// 저장 버튼 클릭 이벤트
saveBtn.addEventListener("click", () => {
    const rows = table.querySelectorAll("tbody tr");
    const data = [];

    rows.forEach(row => {
        const cells = row.querySelectorAll("td");
        const rowData = {
            scheduleNo: cells[0].innerText.trim(),
            schedule_A: cells[1].innerText.trim(),
            schedule_B: cells[2].innerText.trim(),
            schedule_C: cells[3].innerText.trim(),
            schedule_D: cells[4].innerText.trim(),
            schedule_E: cells[5].innerText.trim(),
            schedule_F: cells[6].innerText.trim(),
            schedule_G: cells[7].innerText.trim(),
            schedule_H: cells[8].innerText.trim()
        };
        data.push(rowData);
    });

    // 수정한 데이터 JSON POST 방식으로 전송
    fetch("/api/modify-schedule", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(data)
    }).then(res => res.text())
        .then(result => {
            alert("저장 완료!");
            // 수정 종료 상태로 전환
            table.querySelectorAll("td[contenteditable]").forEach(td => {
                td.setAttribute("contenteditable", "false");
                td.style.backgroundColor = ""; // 배경 제거
            });
            saveBtn.style.display = "none";
            editBtn.style.display = "inline-block";

        }).catch(err => {
        console.error(err);
        alert("저장 실패");
    });
});

// 시간표 테이블 렌더링
function renderScheduleTable(schedules) {
    table.style.display = "table";
    tbody.innerHTML = "";

    schedules.forEach(s => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${s.scheduleNo}</td>
            <td contenteditable="false">${s.schedule_A}</td>
            <td contenteditable="false">${s.schedule_B}</td>
            <td contenteditable="false">${s.schedule_C}</td>
            <td contenteditable="false">${s.schedule_D}</td>
            <td contenteditable="false">${s.schedule_E}</td>
            <td contenteditable="false">${s.schedule_F}</td>
            <td contenteditable="false">${s.schedule_G}</td>
            <td contenteditable="false">${s.schedule_H}</td>
        `;
        tbody.appendChild(row);
    });
}



