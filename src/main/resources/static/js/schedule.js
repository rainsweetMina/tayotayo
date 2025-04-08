// 기본 날짜 설정
const today = new Date().toISOString().split('T')[0];
const runDate = document.getElementById("runDate");
runDate.min = today;
runDate.value = today;

// DOM 요소 참조
const editBtn = document.getElementById("editBtn");
const saveBtn = document.getElementById("saveBtn");
const addRowBtn = document.getElementById("addRowBtn");
const deleteRowBtn = document.getElementById("deleteRowBtn");
const table = document.getElementById("schedule-table");
const tbody = document.getElementById("schedule-tbody");
const routeNoteWrapper = document.getElementById("routeNoteWrapper");
routeNoteWrapper.style.display = "none";

// 전역 선언
let selectedStops = [];     // 맵 선택
let routeMapData = [];      // 전체 노선 정보
let deletedRowIds = [];     // 행 삭제
let currentRouteId = "";   // 선택된 routeId
let moveDir = null;          // 노선 방향

// 노선 선택 시 방면 조회
document.getElementById("routeNo").addEventListener("change", () => {
    const routeNo = document.getElementById("routeNo").value;
    // 이전 moveDir 선택박스 제거
    document.getElementById("moveDirWrapper")?.remove();

    // 방면 초기화
    const routeNoteSelect = document.getElementById("routeNote");
    routeNoteSelect.innerHTML = "";
    routeNoteWrapper.style.display = "none";

    fetch(`/api/route-notes?routeNo=${routeNo}`)
        .then(res => res.json())
        .then(notes => {
            const validNotes = notes.filter(n => n && n.trim() !== "");

            if (validNotes.length === 0) {
                loadMoveDirSelector(routeNo);
                return;
            }

            // 방면 select 옵션 채우기
            const defaultOption = new Option("방면 선택", "", true, true);
            defaultOption.disabled = true;
            routeNoteSelect.appendChild(defaultOption);

            validNotes.forEach(note => {
                routeNoteSelect.appendChild(new Option(note, note));
            });

            routeNoteWrapper.style.display = "inline-block";
        });
});

// 방면 선택 시 시간표 & 노선도 호출
document.getElementById("routeNote").addEventListener("change", () => {
    const routeNo = document.getElementById("routeNo").value;
    const routeNote = document.getElementById("routeNote").value || "";

    table.style.display = "none";
    tbody.innerHTML = "";

    if (!routeNo) return;

    const isMoveDir = routeNote === "0" || routeNote === "1";
    const url = isMoveDir
        ? `/api/route-id/by-movedir?routeNo=${routeNo}&moveDir=${routeNote}`
        : `/api/route-id?routeNo=${routeNo}&routeNote=${routeNote}`;

    fetch(url)
        .then(res => res.text())
        .then(routeId => {
            if (routeId) {
                currentRouteId = routeId;
                loadSchedule(routeNo, isMoveDir ? "" : routeNote);
                loadRouteMap(routeId, isMoveDir ? routeNote : null);
            } else {
                console.warn("해당 routeId를 찾을 수 없습니다.");
            }
        });
});

// 방향 선택 생성
function loadMoveDirSelector(routeNo) {
    routeNoteWrapper.style.display = "none";
    document.getElementById("moveDirWrapper")?.remove();

    const wrapper = document.createElement("div");
    wrapper.id = "moveDirWrapper";
    wrapper.innerHTML = `
        <label for="moveDirSelect">방향 선택:</label>
        <select id="moveDirSelect">
            <option value="" disabled selected>방향 선택</option>
            <option value="0">정방향</option>
            <option value="1">역방향</option>
        </select>
    `;
    document.getElementById("routeNo").parentElement.after(wrapper);

    document.getElementById("moveDirSelect").addEventListener("change", () => {
        moveDir = document.getElementById("moveDirSelect").value;
        fetch(`/api/route-id/by-movedir?routeNo=${routeNo}&moveDir=${moveDir}`)
            .then(res => res.text())
            .then(routeId => {
                table.style.display = "none";
                tbody.innerHTML = "";

                if (!routeId || routeId.includes("html")) {
                    alert("해당 방향의 노선이 없습니다");
                    return;
                }
                currentRouteId = routeId;
                loadSchedule(routeNo, "", moveDir);
                loadRouteMap(routeId, moveDir);
            });
    });
}

// 시간표 로딩
function loadSchedule(routeNo, routeNote = "", moveDir = "") {
    const params = new URLSearchParams({
        routeNo,
        ...(routeNote && {routeNote}),
        ...(moveDir && {moveDir})
    });

    fetch(`/api/schedules?${params.toString()}`)
        .then(res => res.json())
        .then(renderScheduleTable);
}

// 스케줄 데이터 조회
function renderScheduleTable(schedules) {
    // DB에 스케줄 데이터가 없을 시 숨김처리
    if (!schedules || schedules.length === 0) {
        table.style.display = "none";
        return;
    }

    table.style.display = "table";
    tbody.innerHTML = "";

    schedules.forEach(s => {
        const row = document.createElement("tr");
        row.setAttribute("data-id", s.id);
        row.innerHTML = `
                <td>${s.scheduleNo}</td>
                <td contenteditable="false">${s.schedule_A ?? ""}</td>
                <td contenteditable="false">${s.schedule_B ?? ""}</td>
                <td contenteditable="false">${s.schedule_C ?? ""}</td>
                <td contenteditable="false">${s.schedule_D ?? ""}</td>
                <td contenteditable="false">${s.schedule_E ?? ""}</td>
                <td contenteditable="false">${s.schedule_F ?? ""}</td>
                <td contenteditable="false">${s.schedule_G ?? ""}</td>
                <td contenteditable="false">${s.schedule_H ?? ""}</td>`;
        tbody.appendChild(row);
    });
}

// 노선도 로딩
function loadRouteMap(routeId, moveDir = null) {
    let url = `/api/route-map?routeId=${routeId}`;
    if (moveDir !== null) {
        url += `&moveDir=${moveDir}`;
    }

    fetch(url)
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
                    stopContainer.dataset.seq = stop.seq;

                    const circle = document.createElement("div");
                    circle.className = "circle";
                    circle.dataset.bsNm = stop.bsNm;

                    const name = document.createElement("div");
                    name.className = "stop-name";
                    name.textContent = stop.bsNm;

                    stopContainer.append(circle, name);
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
            loadHeaderStops(routeId, moveDir);

            // 수정 활성화 상태에서 노선&방면을 바꿀 시 다시 디폴트 값으로 전환
            saveBtn.style.display = "none";
            editBtn.style.display = "inline-block";
            addRowBtn.style.display = "none";
            deleteRowBtn.style.display = "none";

        });
}

// 저장된 정류장 리스트를 서버에서 불러옴 (시작 종점 + 선택 정거장 + 끝 종점)
function loadHeaderStops(routeId, moveDir = null) {
    const params = new URLSearchParams({routeId});
    if (moveDir !== null) params.append("moveDir", moveDir);

    fetch(`/api/schedule-header?${params.toString()}`)
        .then(res => res.json())
        .then(data => {
            selectedStops = Array.isArray(data) ? data : [];
            updateScheduleHeader();
            highlightSelectedCircles();
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

// 클릭기능 정거장 설정
function highlightSelectableCircles() {
    const allCircles = document.querySelectorAll(".circle");
    let minSeq = Infinity;
    let maxSeq = -Infinity;
    // let startCircle = null;
    // let endCircle = null;

    // 시작 / 끝 seq 탐색
    allCircles.forEach(circle => {
        const seq = parseInt(circle.parentElement.dataset.seq);
        minSeq = Math.min(minSeq, seq);
        maxSeq = Math.max(maxSeq, seq);
    });

    allCircles.forEach(circle => {
        const seq = parseInt(circle.parentElement.dataset.seq);
        circle.classList.remove("selectable");
        circle.removeEventListener("click", onCircleClick);

        if (seq !== minSeq && seq !== maxSeq) {
            circle.classList.add("selectable");
            circle.addEventListener("click", onCircleClick);
        }
    });
}

// 원 클릭 함수
function onCircleClick(e) {
    const circle = e.currentTarget;
    const seq = parseInt(circle.parentElement.dataset.seq);
    const allSeqs = [...document.querySelectorAll(".circle")].map(c => parseInt(c.parentElement.dataset.seq));
    const minSeq = Math.min(...allSeqs);
    const maxSeq = Math.max(...allSeqs);

    if (seq === minSeq || seq === maxSeq) return;

    const middleStops = selectedStops.filter(s => s !== minSeq && s !== maxSeq);
    const isSelected = selectedStops.includes(seq);

    if (isSelected) {
        selectedStops = selectedStops.filter(s => s !== seq);
        circle.classList.remove("selected");
    } else {
        if (middleStops.length >= 6) {
            alert("더 이상 선택할 수 없습니다.");
            return;
        }
        selectedStops.push(seq);
        circle.classList.add("selected");
    }

    updateScheduleHeader();
}

// 스케줄 테이블 헤더 업데이트
function updateScheduleHeader() {
    const theadRow = document.getElementById("schedule-thead").querySelector("tr");
    while (theadRow.children.length > 1) {
        theadRow.removeChild(theadRow.lastChild);
    }

    const sortedStops = [...selectedStops].sort((a, b) => a - b);
    sortedStops.forEach(seq => {
        const stop = routeMapData.find(stop => stop.seq === seq);
        const th = document.createElement("th");
        th.textContent = stop ? stop.bsNm : `정류장(${seq})`;
        theadRow.appendChild(th);
    });
}

// 저장
saveBtn.addEventListener("click", () => {
    // 스케줄 테이블 저장
    const rows = table.querySelectorAll("tbody tr");
    const data = [];
    rows.forEach(row => {
        const cells = row.querySelectorAll("td");
        const isNewRow = row.getAttribute("data-new") === "true";
        const id = row.getAttribute("data-id");

        const rowData = {
            id: isNewRow ? null : (id ? parseInt(id) : null),
            scheduleNo: parseInt(cells[0].innerText.trim()),
            schedule_A: cells[1].innerText.trim(),
            schedule_B: cells[2].innerText.trim(),
            schedule_C: cells[3].innerText.trim(),
            schedule_D: cells[4].innerText.trim(),
            schedule_E: cells[5].innerText.trim(),
            schedule_F: cells[6].innerText.trim(),
            schedule_G: cells[7].innerText.trim(),
            schedule_H: cells[8].innerText.trim(),
            routeId: currentRouteId,
            moveDir: moveDir || "",
        };
        data.push(rowData);
    });

    fetch("/api/modify-schedule", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            schedules: data,
            deletedIds: deletedRowIds
        })
    }).then(res => res.text())
        .then(() => {
            alert("저장 완료!");
            deletedRowIds = [];

            table.querySelectorAll("td[contenteditable]").forEach(td => {
                td.setAttribute("contenteditable", "false");
                td.style.backgroundColor = "";
            });
            saveBtn.style.display = "none";
            editBtn.style.display = "inline-block";
            addRowBtn.style.display = "none";
            deleteRowBtn.style.display = "none";
            location.reload();
        });

    // stopOrder 저장 (노선 중 지정 정거장)
    const allStops = [...document.querySelectorAll(".stop-container")];
    let minSeq = Infinity;
    let maxSeq = -Infinity;

    allStops.forEach(stop => {
        const seq = parseInt(stop.dataset.seq);
        minSeq = Math.min(minSeq, seq);
        maxSeq = Math.max(maxSeq, seq);
    });

    const pureMiddleStops = selectedStops.filter(seq => seq !== minSeq && seq !== maxSeq);
    if (pureMiddleStops.length === 6) {
        const stopOrder = [minSeq, ...pureMiddleStops, maxSeq];
        fetch("/api/schedule-header", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                routeId: currentRouteId,
                moveDir: moveDir,
                stopOrder: stopOrder
            })
        });
    }
});

// 수정버튼
editBtn.addEventListener("click", () => {
    // 스케줄 데이터가 없을 경우 숨김표시 상태에서 수정버튼 누르면 노출
    table.style.display = "inline-block";

    table.querySelectorAll("td[contenteditable]").forEach(td => {
        td.setAttribute("contenteditable", "true");
        td.style.backgroundColor = "#fff8dc";
    });
    highlightSelectedCircles();
    highlightSelectableCircles();
    editBtn.style.display = "none";
    saveBtn.style.display = "inline-block";
    // 행 추가&제거 버튼
    addRowBtn.style.display = "inline-block";
    deleteRowBtn.style.display = "inline-block";
});

// 행 추가 버튼
document.getElementById("addRowBtn").addEventListener("click", () => {
    const tbody = document.getElementById("schedule-tbody");

    // 현재 있는 scheduleNo들 중 최대값 찾기
    const maxNo = Math.max(
        0,
        ...Array.from(tbody.querySelectorAll("tr")).map(tr =>
            parseInt(tr.querySelector("td")?.innerText || "0")
        )
    );

    const newRow = document.createElement("tr");
    newRow.innerHTML = `
        <td>${maxNo + 1}</td>
        <td contenteditable="true"></td>
        <td contenteditable="true"></td>
        <td contenteditable="true"></td>
        <td contenteditable="true"></td>
        <td contenteditable="true"></td>
        <td contenteditable="true"></td>
        <td contenteditable="true"></td>
        <td contenteditable="true"></td>
    `;
    newRow.setAttribute("data-new", "true");
    newRow.setAttribute("data-id", "");
    tbody.appendChild(newRow);
});

// 행 삭제 버튼
document.getElementById("deleteRowBtn").addEventListener("click", () => {
    const rows = document.querySelectorAll("#schedule-tbody tr");
    if (rows.length === 0) {
        alert("삭제할 행이 없습니다.");
        return;
    }
    const lastRow = rows[rows.length - 1]
    const isNewRow = lastRow.getAttribute("data-new") === "true";
    const rowId = lastRow.getAttribute("data-id");

    if (isNewRow || confirm("정말 삭제하시겠습니까?")) {
        if (!isNewRow && rowId) {
            deletedRowIds.push(parseInt(rowId));
        }
            lastRow.remove();
    }
})