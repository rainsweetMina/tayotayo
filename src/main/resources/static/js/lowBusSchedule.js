// 전역 변수 선언
// let currentRouteId = "";   // 선택된 routeId
// let selectedStops = [];     // 헤더 정류장 seq 저장용
// let routeMapData = [];

// 노선 선택 시
document.getElementById("routeNo")?.addEventListener("click", async () => {
    const routeNo = document.getElementById("routeNo").value;
    document.getElementById("schedule-container").innerHTML = "";

    const notesRes = await fetch(`/api/route-notes?routeNo=${routeNo}`);
    const routeNotes = await notesRes.json();
    const validNotes = routeNotes.filter(n => n && n.trim() !== "");

    if (validNotes.length > 0) {
        // 방면으로 조회
        for (const note of validNotes) {
            const routeIdRes = await fetch(`/api/route-id?routeNo=${routeNo}&routeNote=${note}`);
            const routeId = await routeIdRes.text();

            if (routeId) {
                await renderScheduleSection(routeId, routeNo, note, null);
            }
        }
    } else {
        // 방향으로 조회
        for (const dir of [0, 1]) {
            const routeIdRes = await fetch(`/api/route-id/by-movedir?routeNo=${routeNo}&moveDir=${dir}`);
            const routeId = await routeIdRes.text();

            if (routeId) {
                await renderScheduleSection(routeId, routeNo, "", dir);
            }
        }
    }
});

async function renderScheduleSection(routeId, routeNo, routeNote = "", moveDir = null) {
    // 정류장 헤더 불러오기
    const headerRes = await fetch(`/api/schedule-header?routeId=${routeId}${moveDir !== null ? `&moveDir=${moveDir}` : ""}`);
    const headerSeq = await headerRes.json();

    if (!Array.isArray(headerSeq) || headerSeq.length === 0) return;

    // 정류장 이름용 routeMap
    const routeMapRes = await fetch(`/api/route-map?routeId=${routeId}${moveDir !== null ? `&moveDir=${moveDir}` : ""}`);
    const mapData = await routeMapRes.json();

    // 스케줄 데이터 (저상버스만 조회)
    const params = new URLSearchParams({routeNo});
    if (routeNote) params.append("routeNote", routeNote);
    if (moveDir !== null) params.append("moveDir", moveDir);
    const scheduleRes = await fetch(`/api/schedules?${params.toString()}`);
    const schedules = (await scheduleRes.json()).filter(s => s.busTCd === "D");
    if (schedules.length === 0) return;

    // 헤더 이름 변환
    const headerNames = headerSeq.map((seq, idx) => {
        const stop = mapData.find(stop => stop.seq === seq);
        if (idx === 0) return (stop?.bsNm || "출발");
        if (idx === headerSeq.length - 1) return (stop?.bsNm || "도착");
        return stop?.bsNm || `중간${idx}`;
    });

    // DOM 구성
    const container = document.getElementById("schedule-container");
    const section = document.createElement("div");
    section.className = "schedule-section";

    const title = document.createElement("h3");
    title.textContent = routeNote ? `${routeNote}` : (moveDir === 0 ? "정방향" : "역방향");
    section.appendChild(title);

    const table = document.createElement("table");
    table.classList.add("schedule-table");

    // 테이블 헤드
    const theed = document.createElement("thead")
    const headRow = document.createElement("tr");
    headRow.innerHTML = `<th>회차</th>` + headerNames.map(name => `<th>${name}</th>`).join("");
    theed.append(headRow);
    table.appendChild(theed);

    // 테이블 바디
    const tbody = document.createElement("tbody");
    schedules.forEach((s, index) => {
        const row = document.createElement("tr");
        const cells = [
            index + 1,
            s.schedule_A, s.schedule_B, s.schedule_C, s.schedule_D,
            s.schedule_E, s.schedule_F, s.schedule_G, s.schedule_H
            ].map(value => `<td>${value ?? ""}</td>`).join("");
        row.innerHTML = cells;
        tbody.appendChild(row);
    })

    table.appendChild(tbody);
    section.appendChild(table);
    container.appendChild(section);

}
