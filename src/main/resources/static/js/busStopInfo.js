
document.addEventListener("DOMContentLoaded", () => {
    history.replaceState(null, "", "/bus-info");

    const districtSelect = document.getElementById("districtSelect");
    const neighborhoodSelect = document.getElementById("neighborhoodSelect");
    const routeTypeSelect = document.getElementById("routeTypeSelect");
    const routeNoSelect = document.getElementById("routeNoSelect");
    const resultTable = document.getElementById("resultTable");
    const resultBody = document.getElementById("resultBody");

    const regionTab = document.getElementById("regionTab");
    const routeTab = document.getElementById("routeTab");
    const regionSearchBox = document.getElementById("regionSearchBox");
    const routeSearchBox = document.getElementById("routeSearchBox");

    const regionStopInput = regionSearchBox.querySelector(".stopNameInput");
    const routeStopInput = routeSearchBox.querySelector(".stopNameInput");

    let currentFetchedData = [];

    // ✅ 탭 전환
    regionTab.addEventListener("click", () => {
        regionSearchBox.style.display = "block";
        routeSearchBox.style.display = "none";
        clearTable();
    });

    routeTab.addEventListener("click", () => {
        regionSearchBox.style.display = "none";
        routeSearchBox.style.display = "block";
        clearTable();
    });

    // ✅ 정류소명 실시간 검색
    regionStopInput.addEventListener("input", () => renderResults(currentFetchedData));
    routeStopInput.addEventListener("input", () => renderResults(currentFetchedData));

    // ✅ 노선 유형 선택 시 → 노선번호 목록 불러오기
    routeTypeSelect.addEventListener("change", () => {
        const type = routeTypeSelect.value;
        routeNoSelect.innerHTML = `<option value="" disabled selected>-- 노선 선택 --</option>`;
        clearTable();

        fetch(`/api/bus-info/route-nos?type=${encodeURIComponent(type)}`)
            .then(res => res.json())
            .then(data => {
                data.forEach(routeNo => {
                    const opt = document.createElement("option");
                    opt.value = routeNo;
                    opt.textContent = routeNo;
                    routeNoSelect.appendChild(opt);
                });
            })
            .catch(err => console.error("노선 목록 불러오기 오류:", err));
    });

    // ✅ 노선 선택 시 → 정류소 목록 불러오기
    routeNoSelect.addEventListener("change", () => {
        const routeNo = routeNoSelect.value;
        clearTable();

        fetch(`/api/bus-info/search-by-route?routeNo=${encodeURIComponent(routeNo)}`)
            .then(res => res.json())
            .then(renderResults)
            .catch(err => console.error("노선별 정류소 조회 오류:", err));
    });

    // ✅ 구 선택 시 → 동 목록 + 정류소 전체 조회
    districtSelect.addEventListener("change", () => {
        const district = districtSelect.value;
        neighborhoodSelect.innerHTML = `<option selected disabled>-- 동 선택 --</option>`;
        clearTable();

        fetch(`/api/bus-info/neighborhoods?district=${encodeURIComponent(district)}`)
            .then(res => res.json())
            .then(data => {
                data.forEach(n => {
                    const opt = document.createElement("option");
                    opt.value = n;
                    opt.textContent = n;
                    neighborhoodSelect.appendChild(opt);
                });
            });

        fetch(`/api/bus-info/search?district=${encodeURIComponent(district)}`)
            .then(res => res.json())
            .then(renderResults)
            .catch(e => console.error("구 기준 검색 오류:", e));
    });

    // ✅ 동 선택 시 → 정류소 목록 필터링
    neighborhoodSelect.addEventListener("change", () => {
        const district = districtSelect.value;
        const neighborhood = neighborhoodSelect.value;
        clearTable();

        fetch(`/api/bus-info/search?district=${encodeURIComponent(district)}&neighborhood=${encodeURIComponent(neighborhood)}`)
            .then(res => res.json())
            .then(renderResults)
            .catch(e => console.error("동 기준 검색 오류:", e));
    });

    // ✅ 결과 테이블 초기화
    function clearTable() {
        resultBody.innerHTML = "";
        resultTable.style.display = "none";
    }

    // ✅ 결과 렌더링 (정류소명 필터 포함)
    function renderResults(data) {
        clearTable();
        currentFetchedData = data;

        const keyword = regionSearchBox.style.display !== "none"
            ? regionStopInput.value.trim()
            : routeStopInput.value.trim();

        const filtered = keyword
            ? data.filter(info => {
                // 구조: info.busStop?.bsNm 또는 info.bsNm (케이스별 대응)
                if (info.busStop?.bsNm) return info.busStop.bsNm.includes(keyword);
                if (info.bsNm) return info.bsNm.includes(keyword);
                return false;
            })
            : data;

        if (filtered.length === 0) return;

        const half = Math.ceil(filtered.length / 2);
        const left = filtered.slice(0, half);
        const right = filtered.slice(half);

        for (let i = 0; i < half; i++) {
            const leftInfo = left[i];
            const rightInfo = right[i];

            const row = document.createElement("tr");

            // 왼쪽 열
            row.innerHTML += `
            <td>${leftInfo?.city ?? ''} ${leftInfo?.district ?? ''}</td>
            <td>${leftInfo?.busStop?.bsNm ?? leftInfo?.bsNm ?? ''}</td>
        `;

            // 오른쪽 열
            if (rightInfo) {
                row.innerHTML += `
                <td>${rightInfo?.city ?? ''} ${rightInfo?.district ?? ''}</td>
                <td>${rightInfo?.busStop?.bsNm ?? rightInfo?.bsNm ?? ''}</td>
            `;
            } else {
                row.innerHTML += `<td></td><td></td>`;
            }

            resultBody.appendChild(row);
        }

        resultTable.style.display = "table";
    }
});