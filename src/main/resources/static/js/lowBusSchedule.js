// 전역 변수 선언
let currentRouteId = "";   // 선택된 routeId
let moveDir = null;          // 노선 방향
let selectedStops = [];     // 헤더 정류장 seq 저장용
let routeMapData = [];

// DOM 참조
const table = document.getElementById("schedule-table");
const tbody = document.getElementById("schedule-tbody");

// 노선 선택 시
document.getElementById("routeNo")?.addEventListener("click", (e) => {
    const routeNo = document.getElementById("routeNo").value;
    document.getElementById("moveDirWrapper")?.remove();

    const routeNoteSelect = document.getElementById("routeNote");
    routeNoteSelect.innerHTML = "";
    routeNoteWrapper.style.display = "none";
    fetch(`/api/route-notes?routeNo=${routeNo}`)
        .then(res => res.json())
        .then(data => {
            const routeNoteSelect = document.getElementById("routeNote");
            routeNoteSelect.innerHTML = "";

            const validNotes = data.filter(n => n && n.trim() !== "");

            if (validNotes.length === 0) {
                loadMoveDirSelector(routeNo);
                return;
            }

            const defaultOption = new Option("방면 선택", "", true, true);
            defaultOption.disabled = true;
            routeNoteSelect.appendChild(defaultOption);

            validNotes.forEach(note => {
                routeNoteSelect.appendChild(new Option(note, note));
            });
            document.getElementById("routeNoteWrapper").style.display = "inline-block";
        });
});

// 방면 선택시 스케줄 조회
document.getElementById("routeNote")?.addEventListener("change", () => {
    const routeNo = document.getElementById("routeNo").value;
    const routeNote = document.getElementById("routeNote").value || "";

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
                loadRouteMap(routeId, isMoveDir ? routeNote : null);
                loadLowBusSchedule(routeNo, isMoveDir ? "" : routeNote);
            }
        });
});

// moveDir 있는 경우
function loadMoveDirSelector(routeNo) {
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
                currentRouteId = routeId;
                loadRouteMap(routeId, moveDir);
                loadLowBusSchedule(routeNo, "", moveDir);
            });
    });
}

// 스케줄 로딩 (busTCd = D 필터 포함)
function loadLowBusSchedule(routeNo, routeNote = "", moveDir = "") {
    const params = new URLSearchParams({
        routeNo,
        ...(routeNote && {routeNote}),
        ...(moveDir && {moveDir})
    });

    fetch(`/api/schedules?${params.toString()}`)
        .then(res => res.json())
        .then(schedules => {
            const lowBusOnly = schedules.filter(s => s.busTCd === "D");
            loadHeaderStops(currentRouteId, moveDir);

            // ✅ 테이블 데이터 렌더링
            renderScheduleTable(lowBusOnly);
        });
}

// 테이블 헤드 가져오기
function loadHeaderStops(routeId, moveDir = null) {
    const params = new URLSearchParams({ routeId });
    if (moveDir !== null) params.append("moveDir", moveDir);

    fetch(`/api/schedule-header?${params.toString()}`)
        .then(res => res.json())
        .then(data => {
            if (Array.isArray(data)) {
                selectedStops = data;
                updateScheduleHeader();  // 👉 이 함수에서 테이블 헤더 렌더링
            } else {
                console.warn("정류장 리스트를 불러오지 못했습니다.");
            }
        });
}

function updateScheduleHeader() {
    const theadRow = document.getElementById("schedule-thead").querySelector("tr");

    // 기존 헤더 초기화 (회차 칸 제외)
    while (theadRow.children.length > 1) {
        theadRow.removeChild(theadRow.lastChild);
    }

    selectedStops.forEach((seq, idx) => {
        const stop = routeMapData.find(stop => stop.seq === seq); // 🧠 routeMapData 필수
        const th = document.createElement("th");

        if (idx === 0) th.textContent = stop ? stop.bsNm + " (출발)" : "출발";
        else if (idx === selectedStops.length - 1) th.textContent = stop ? stop.bsNm + " (도착)" : "도착";
        else th.textContent = stop ? stop.bsNm : `중간${idx}`;

        theadRow.appendChild(th);
    });
}

function loadRouteMap(routeId, moveDir = null) {
    let url = `/api/route-map?routeId=${routeId}`;
    if (moveDir !== null) url += `&moveDir=${moveDir}`;

    fetch(url)
        .then(res => res.json())
        .then(data => {
            routeMapData = data; // ⭐ 전역에 저장
            loadHeaderStops(routeId, moveDir); // 💡 정류장 순서 불러오기
        });
}


// 화면에 테이블 렌더링
function renderScheduleTable(schedules) {
    // if (!schedules || schedules.length === 0) {
    //     table.style.display = "none";
    //     return;
    // }

    table.style.display = "table";
    tbody.innerHTML = "";

    schedules.forEach((s, index) => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${index + 1}</td>
            <td>${s.schedule_A ?? ""}</td>
            <td>${s.schedule_B ?? ""}</td>
            <td>${s.schedule_C ?? ""}</td>
            <td>${s.schedule_D ?? ""}</td>
            <td>${s.schedule_E ?? ""}</td>
            <td>${s.schedule_F ?? ""}</td>
            <td>${s.schedule_G ?? ""}</td>
            <td>${s.schedule_H ?? ""}</td>
        `;
        tbody.appendChild(row);
    });
}