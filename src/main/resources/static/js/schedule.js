const today = new Date().toISOString().split('T')[0];
document.getElementById("runDate").min = today;
document.getElementById("runDate").value = today;

const editBtn = document.getElementById("editBtn");
const saveBtn = document.getElementById("saveBtn");
const table = document.getElementById("schedule-table");

document.getElementById("routeNo").addEventListener("change", function () {
    const routeNo = this.value;

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

editBtn.addEventListener("click", () => {
    table.querySelectorAll("td[contenteditable]").forEach(td => {
        td.setAttribute("contenteditable", "true");
        td.style.backgroundColor = "#fff8dc"; // 편집 시 표시
    });
    editBtn.style.display = "none";
    saveBtn.style.display = "inline-block";
});

saveBtn.addEventListener("click", () => {
    const rows = table.querySelectorAll("tbody tr");
    const data = [];

    rows.forEach(row => {
        const cells = row.querySelectorAll("td");
        const rowData = {
            schedule_no: cells[0].innerText.trim(),
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

    // 서버 전송
    fetch("/api/save-schedule-bulk", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
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