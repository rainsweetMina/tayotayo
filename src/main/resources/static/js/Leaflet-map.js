// ✅ Leaflet 지도 초기화 후 전역 저장 (이제는 leafletMap으로 명확히 구분)
window.leafletMap = L.map('map').setView([35.8668, 128.5940], 13);

// ✅ OpenStreetMap 타일 레이어 추가
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap'
}).addTo(window.leafletMap);

// ✅ 정류장 마커 저장 배열 (전역)
window.busStopMarkers = [];

/**
 * 정류장 마커를 지도에 표시
 * @param {Array} stopData - 정류장 데이터 리스트
 */
function drawBusStopMarkers(map, stopData) {
    window.busStopMarkers.forEach(marker => map.removeLayer(marker));
    window.busStopMarkers = [];

    if (!Array.isArray(stopData)) return;

    stopData.forEach(stop => {
        const lat = parseFloat(stop.ypos);
        const lng = parseFloat(stop.xpos);

        if (!isFinite(lat) || !isFinite(lng)) return;

        const marker = L.marker([lat, lng], {
            icon: L.icon({
                iconUrl: '/img/bus-stop-icon.png',
                iconSize: [44, 44],
                iconAnchor: [12, 24]
            }),
            title: stop.bsNm
        }).addTo(map);

        marker.on('click', () => {
            fetch(`/api/bus/bus-arrival?bsId=${stop.bsId}`)
                .then(res => res.json())
                .then(data => {
                    const body = data.body;
                    if (body.totalCount === 0 || !body.items) {
                        marker.bindPopup(`<b>${stop.bsNm}</b><br>도착 예정 정보 없음`).openPopup();
                        return;
                    }

                    let content = `<b>${stop.bsNm}</b><br><br>`;

                    const items = Array.isArray(body.items) ? body.items : [body.items];
                    items.forEach(item => {
                        const arrList = Array.isArray(item.arrList) ? item.arrList : [item.arrList];
                        arrList.forEach(arr => {
                            content += `🚌 <b>${item.routeNo}</b>: ${arr.arrState}<br>`;
                        });
                    });

                    marker.bindPopup(content).openPopup();
                })
                .catch(err => {
                    marker.bindPopup(`<b>${stop.bsNm}</b><br>정보 조회 실패`).openPopup();
                    console.error("❌ 도착 정보 요청 실패:", err);
                });
        });
        window.busStopMarkers.push(marker);
    });
}


function drawRouteBusStopMarkers(map, stopData) {
    // 기존 마커 제거
    window.busStopMarkers.forEach(marker => map.removeLayer(marker));
    window.busStopMarkers = [];

    if (!Array.isArray(stopData)) return;

    stopData.forEach(stop => {
        const lat = parseFloat(stop.yPos);
        const lng = parseFloat(stop.xPos);

        if (!isFinite(lat) || !isFinite(lng)) return;

        const marker = L.marker([lat, lng], {
            icon: L.icon({
                iconUrl: '/img/bus-stop-icon.png',
                iconSize: [45, 45],
                iconAnchor: [12, 24]
            }),
            title: stop.bsNm
        }).addTo(map);

        // 🚌 마커 클릭 시 실시간 도착 정보 표시
        marker.on('click', () => {
            fetch(`/api/bus/bus-arrival?bsId=${stop.bsId}`)
                .then(res => res.json())
                .then(data => {
                    const body = data.body;
                    if (body.totalCount === 0 || !body.items) {
                        marker.bindPopup(`<b>${stop.bsNm}</b><br>도착 정보 없음`).openPopup();
                        return;
                    }

                    let content = `<b>${stop.bsNm}</b><br><br>`;
                    const items = Array.isArray(body.items) ? body.items : [body.items];

                    items.forEach(item => {
                        const arrList = Array.isArray(item.arrList) ? item.arrList : [item.arrList];
                        arrList.forEach(arr => {
                            content += `🚌 <b>${item.routeNo}</b>: ${arr.arrState}<br>`;
                        });
                    });

                    marker.bindPopup(content).openPopup();
                })
                .catch(err => {
                    marker.bindPopup(`<b>${stop.bsNm}</b><br>도착 정보 조회 실패`).openPopup();
                    console.error("❌ 도착 정보 조회 에러:", err);
                });
        });

        window.busStopMarkers.push(marker);
    });
}

