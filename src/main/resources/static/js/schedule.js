// 기본 날짜 설정
const today = new Date().toISOString().split('T')[0];
const runDate = document.getElementById("runDate");
runDate.min = today;
runDate.value = today;

const editBtn = document.getElementById("editBtn");
const saveBtn = document.getElementById("saveBtn");
const table = document.getElementById("schedule-table");
const tbody = document.getElementById("schedule-tbody");

document.getElementById("routeNoteLabel").style.display = "none";
document.getElementById("routeNote").style.display = "none";

// 노선 선택 시 방면 목록 가져오기
document.getElementById("routeNo").addEventListener("change", () => {
    const routeNo = document.getElementById("routeNo").value;

    const routeNoteSelect = document.getElementById("routeNote");
    const routeNoteLabel = document.getElementById("routeNoteLabel");

    // ✅ 초기에는 아예 숨기고 시작
    routeNoteLabel.style.display = "none";
    routeNoteSelect.style.display = "none";
    routeNoteSelect.innerHTML = "";

    fetch(`/api/route-notes?routeNo=${routeNo}`)
        .then(res => res.json())
        .then(data => {
            const validNotes = data.filter(note => note && note.trim() !== "");

            if (validNotes.length === 0) {
                // ❌ 방면 없음 → 그대로 숨기고 바로 routeId 처리
                fetch(`/api/route-id?routeNo=${routeNo}&routeNote=`)
                    .then(res => res.text())
                    .then(routeId => {
                        if (routeId) {
                            loadSchedule(routeNo, "");
                            loadRouteMap(routeId);
                        }
                    });
            } else {
                // ✅ 방면 있음 → select 구성 후 보여주기
                const defaultOption = document.createElement("option");
                defaultOption.value = "";
                defaultOption.textContent = "방면 선택";
                defaultOption.disabled = true;
                defaultOption.selected = true;
                routeNoteSelect.appendChild(defaultOption);

                validNotes.forEach(note => {
                    const option = document.createElement("option");
                    option.value = note;
                    option.textContent = note;
                    routeNoteSelect.appendChild(option);
                });

                // ✅ fetch 이후에만 보이게 함
                routeNoteLabel.style.display = "inline-block";
                routeNoteSelect.style.display = "inline-block";
            }
        });
});


// 방면 선택 시 시간표 & 노선도 호출
document.getElementById("routeNote").addEventListener("change", () => {
    const routeNo = document.getElementById("routeNo").value;
    let routeNote = document.getElementById("routeNote").value;

    if (!routeNo) return;
    if (!routeNote || routeNote === "방면 없음") routeNote = "";

    fetch(`/api/route-id?routeNo=${routeNo}&routeNote=${routeNote}`)
        .then(res => res.text())
        .then(routeId => {
            if (routeId) {
                loadSchedule(routeNo, routeNote);
                loadRouteMap(routeId);
            } else {
                console.warn("해당 routeId를 찾을 수 없습니다.");
            }
        });
});

// 시간표 로딩
function loadSchedule(routeNo, routeNote) {
    fetch(`/api/schedules?routeNo=${routeNo}&routeNote=${routeNote}`)
        .then(res => res.json())
        .then(renderScheduleTable);
}

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
                <td contenteditable="false">${s.schedule_H}</td>`;
        tbody.appendChild(row);
    });
}

// 노선도 로딩
function loadRouteMap(routeId) {
    fetch(`/api/route-map?routeId=${routeId}`)
        .then(res => res.json())
        .then(data => {
            const mapContainer = document.getElementById("route-map");
            mapContainer.innerHTML = "";

            const stopsPerLine = 8;

            for (let i = 0; i < data.length; i += stopsPerLine) {
                const line = document.createElement("div");
                line.className = "route-line";

                const chunk = data.slice(i, i + stopsPerLine);
                chunk.forEach((stop, index) => {
                    const stopContainer = document.createElement("div");
                    stopContainer.className = "stop-container";

                    const circle = document.createElement("div");
                    circle.className = "circle";

                    const name = document.createElement("div");
                    name.className = "stop-name";
                    name.textContent = stop.bsNm;

                    stopContainer.appendChild(circle);
                    stopContainer.appendChild(name);
                    line.appendChild(stopContainer);

                    if (index < chunk.length - 1) {
                        const connector = document.createElement("div");
                        connector.className = "connector";
                        line.appendChild(connector);
                    }
                });

                mapContainer.appendChild(line);
            }
        })
        .catch(err => console.error("노선도 불러오기 실패:", err));
}


// 수정 → 저장 버튼 로직
editBtn.addEventListener("click", () => {
    table.querySelectorAll("td[contenteditable]").forEach(td => {
        td.setAttribute("contenteditable", "true");
        td.style.backgroundColor = "#fff8dc";
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

    fetch("/api/modify-schedule", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(data)
    }).then(res => res.text())
        .then(result => {
            alert("저장 완료!");
            table.querySelectorAll("td[contenteditable]").forEach(td => {
                td.setAttribute("contenteditable", "false");
                td.style.backgroundColor = "";
            });
            saveBtn.style.display = "none";
            editBtn.style.display = "inline-block";
        })
        .catch(err => {
            console.error(err);
            alert("저장 실패");
        });
});