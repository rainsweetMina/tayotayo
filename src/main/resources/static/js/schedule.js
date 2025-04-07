// 기본 날짜 설정
const today = new Date().toISOString().split('T')[0];
const runDate = document.getElementById("runDate");
runDate.min = today;
runDate.value = today;

const editBtn = document.getElementById("editBtn");
const saveBtn = document.getElementById("saveBtn");
const table = document.getElementById("schedule-table");
const tbody = document.getElementById("schedule-tbody");

document.getElementById("routeNoteWrapper").style.display = "none";

let selectedStops = [];
let routeMapData = [];
let currentRouteId = "";

// 노선 선택 시 방면 목록 가져오기
document.getElementById("routeNo").addEventListener("change", () => {
    const routeNo = document.getElementById("routeNo").value;
    const routeNoteSelect = document.getElementById("routeNote");
    const routeNoteWrapper = document.getElementById("routeNoteWrapper");

    // 해당 노선의 방면 선택이 없는 경우를 위해 숨김을 기본값으로 설정
    routeNoteWrapper.style.display = "none";
    routeNoteSelect.innerHTML = "";

    fetch(`/api/route-notes?routeNo=${routeNo}`)
        .then(res => res.json())
        .then(data => {
            const validNotes = data.filter(note => note && note.trim() !== "");

            if (validNotes.length === 0) {
                // 선택 노선의 방면이 없는 경우 그대로 숨기고 바로 routeId 바로 조회
                fetch(`/api/route-id?routeNo=${routeNo}&routeNote=`)
                    .then(res => res.text())
                    .then(routeId => {
                        if (routeId) {
                            currentRouteId = routeId;
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
                routeNoteWrapper.style.display = "inline-block";
                routeNoteSelect.style.display = "inline-block";
            }
        });
});

// 방면 선택 시 시간표 & 노선도 호출
document.getElementById("routeNote").addEventListener("change", () => {
    const routeNo = document.getElementById("routeNo").value;
    const routeNote = document.getElementById("routeNote").value || "";

    if (!routeNo) return;

    fetch(`/api/route-id?routeNo=${routeNo}&routeNote=${routeNote}`)
        .then(res => res.text())
        .then(routeId => {
            if (routeId) {
                currentRouteId = routeId;
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
    // DB에 스케줄 데이터가 없을 시 숨김처리
    // if (!schedules || schedules.length === 0) {
    //     table.style.display = "none";
    //     return;
    // }

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
                    circle.dataset.bsNm = stop.bsNm;
                    stopContainer.dataset.seq = stop.seq;

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
            routeMapData = data;
            loadHeaderStops(routeId);

            // 수정 활성화 상태에서 노선&방면을 바꿀 시 다시 디폴트 값으로 전환
            saveBtn.style.display = "none";
            editBtn.style.display = "inline-block";
        })
        .catch(err => console.error("노선도 불러오기 실패:", err));
}

// 저장된 정류장 리스트를 서버에서 불러옴
function loadHeaderStops(routeId) {
    fetch(`/api/schedule-header?routeId=${encodeURIComponent(routeId)}`)
        .then(res => res.json())
        .then(data => {
            if (!Array.isArray(data)) {
                console.warn("서버로부터 배열이 아님:", data);
                selectedStops = [];
            } else {
                selectedStops = data;
            }
            updateScheduleHeader();
            highlightSelectedCircles();
        })
        .catch(err => {
            console.error("정류장 목록 불러오기 실패:", err);
            selectedStops = [];
        });
}

// 지정 정류장 표시
function highlightSelectedCircles() {
    const allCircles = document.querySelectorAll(".circle");
    let minSeq = Infinity;
    let maxSeq = -Infinity;
    let startCircle = null;
    let endCircle = null;

    // seq 범위 찾기
    allCircles.forEach(circle => {
        const seq = parseInt(circle.parentElement.dataset.seq);
        if (seq < minSeq) {
            minSeq = seq;
            startCircle = circle;
        }
        if (seq > maxSeq) {
            maxSeq = seq;
            endCircle = circle;
        }
    });

    // 시작/끝은 선택 불가
    allCircles.forEach(circle => {
        const seq = parseInt(circle.parentElement.dataset.seq);
        circle.classList.remove("selected", "start-stop", "end-stop");

        if (circle === startCircle) {
            circle.classList.add("start-stop");
        } else if (circle === endCircle) {
            circle.classList.add("end-stop");
        } else if (selectedStops.includes(seq)) {
            circle.classList.add("selected");
        }
    });
}

function highlightSelectableCircles() {
    const allCircles = document.querySelectorAll(".circle");
    let minSeq = Infinity;
    let maxSeq = -Infinity;

    // 시작 / 끝 seq 탐색
    allCircles.forEach(circle => {
        const seq = parseInt(circle.parentElement.dataset.seq);
        if (seq < minSeq) {
            minSeq = seq;
            startCircle = circle;
        }
        if (seq > maxSeq) {
            maxSeq = seq;
            endCircle = circle;
        }
    });

    allCircles.forEach(circle => {
        const seq = parseInt(circle.parentElement.dataset.seq);

        circle.classList.remove("selectable");
        circle.removeEventListener("click", onCircleClick);

        if (circle !== startCircle && circle !== endCircle) {
            circle.classList.add("selectable");
            circle.addEventListener("click", onCircleClick);
        }
    });
}

// 수정 → 저장 버튼 로직
editBtn.addEventListener("click", () => {
    // selectedStops = [];

    table.querySelectorAll("td[contenteditable]").forEach(td => {
        td.setAttribute("contenteditable", "true");
        td.style.backgroundColor = "#fff8dc";
    });
    highlightSelectedCircles();
    highlightSelectableCircles();
    editBtn.style.display = "none";
    saveBtn.style.display = "inline-block";
});

// 원클릭 함수
function onCircleClick(e) {
    const circle = e.currentTarget;
    const seq = parseInt(circle.parentElement.dataset.seq);
    const isSelected = selectedStops.includes(seq);

    if (isSelected) {
        selectedStops = selectedStops.filter(s => s !== seq);
        circle.classList.remove("selected");
    } else {
        if (selectedStops.length >= 6) {
            alert("더 이상 선택하실 수 없습니다.");
            return;
        }
        selectedStops.push(seq);
        circle.classList.add("selected");
    }
    updateScheduleHeader();
}

function updateScheduleHeader() {
    const theadRow = document.getElementById("schedule-thead").querySelector("tr");
    while (theadRow.children.length > 1) {
        theadRow.removeChild(theadRow.lastChild);
    }

    const sortedStops = [...selectedStops].sort((a, b) => a - b);
    sortedStops.forEach(seq => {
        const th = document.createElement("th");
        const stop = routeMapData.find(stop => stop.seq === seq);
        th.textContent = stop ? stop.bsNm : `정류장(${seq})`;
        theadRow.appendChild(th);
    });
}

// 정류장 이름으로 seq 찾는 함수
function getSeqByStopName(name) {
    const container = [...document.querySelectorAll(".stop-container")]
        .find(div => div.querySelector(".circle")?.dataset.bsNm === name);
    return container ? parseInt(container.dataset.seq) : Infinity;
}

// 저장
saveBtn.addEventListener("click", () => {
    if (selectedStops.length !== 6) {
        alert("중간 정류장은 6개를 지정하셔야 합니다..");
        return;
    }

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

    // 시작점/끝점 자동 포함
    const allStops = [...document.querySelectorAll(".stop-container")];
    let firstSeq = null;
    let lastSeq = null;
    let minSeq = Infinity;
    let maxSeq = -Infinity;

    allStops.forEach(stop => {
        const seq = parseInt(stop.dataset.seq);
        if (seq < minSeq) {
            minSeq = seq;
            firstSeq = seq;
        }
        if (seq > maxSeq) {
            maxSeq = seq;
            lastSeq = seq;
        }
    });

    // 최종 저장용 stopOrder 구성
    const sortedSeqStops = [...selectedStops].sort((a, b) => a - b);
    const stopOrder = [firstSeq, ...sortedSeqStops, lastSeq];

    // 시간표 저장
    fetch("/api/modify-schedule", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    }).then(res => res.text())
        .then(() => {
            alert("저장 완료!");
            table.querySelectorAll("td[contenteditable]").forEach(td => {
                td.setAttribute("contenteditable", "false");
                td.style.backgroundColor = "";
            });
            saveBtn.style.display = "none";
            editBtn.style.display = "inline-block";
            location.reload();
        });

    // 정류장 순서 저장
    fetch("/api/schedule-header", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            routeId: currentRouteId,
            stopOrder: stopOrder
        })
    });
});

