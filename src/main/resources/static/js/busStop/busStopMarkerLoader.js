// window.loadBusStopsInView: 전역에서 쓸 수 있게 window에 등록
// map: Leaflet 지도 인스턴스
// markers: 현재 마커들을 저장한 배열 (기존 마커 제거용)
// minZoom: 최소 줌 레벨 (기본값 15) → 이보다 작으면 마커 안 보여줌
// apiUrl: 정류장 정보를 가져올 API 주소
window.loadBusStopsInView = async function (map, markers, minZoom = 16, apiUrl = "/api/bus/busStopsInBounds") {

    // 현재 줌레벨 확인
    // 현재 지도 줌 레벨을 확인해서 minZoom보다 작으면
    // 기존 마커들만 제거하고
    // 새 마커를 불러오지 않음 → 마커 너무 많을 때 성능 최적화 목적
    const currentZoom = map.getZoom();
    if (currentZoom < minZoom) {
        markers.forEach(m => map.removeLayer(m));
        markers.length = 0;
        return;
    }


    // 현재 지도 화면의 좌표 범위 구하기
    const bounds = map.getBounds();
    const sw = bounds.getSouthWest();
    const ne = bounds.getNorthEast();


    // API 호출해서 정류장 데이터 받아오기
    const url = `${apiUrl}?minX=${sw.lng}&minY=${sw.lat}&maxX=${ne.lng}&maxY=${ne.lat}`;
    const res = await fetch(url);
    const data = await res.json();


    // 기존 마커 제거
    // 이미 지도에 찍혀 있던 마커들을 모두 지움
    // markers.length = 0으로 배열도 비움
    markers.forEach(m => map.removeLayer(m));
    markers.length = 0;


    // 새 마커 추가
    data.forEach(stop => {
        const marker = L.marker([stop.ypos, stop.xpos])
            // bindPopup()으로 마커 클릭 시 정류장 이름 보여줌
            .bindPopup(`<strong>${stop.bsNm}</strong>`);
        //지도에 추가하고 markers 배열에 다시 저장
        marker.addTo(map);
        markers.push(marker); // 여기서 기존 배열에 push
    });
};
