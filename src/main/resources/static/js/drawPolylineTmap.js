let currentPolylines = [];

function drawBusRouteMapTmap(data, color = '#007bff') {
    const map = window.kakaoMap;
    const mapContainer = document.getElementById('map');

    if (!mapContainer || !map) {
        console.error("🛑 지도 요소 또는 객체가 존재하지 않습니다.");
        return;
    }

    if (!Array.isArray(data) || data.length === 0) {
        console.warn("⚠️ 경로 데이터가 비어있습니다.");
        return;
    }

    // 🔥 기존 폴리라인 제거
    currentPolylines.forEach(line => line.setMap(null));
    currentPolylines = [];

    // ✅ lat/lng 형식을 kakao.maps.LatLng 객체로 변환
    const path = data.map(p => new kakao.maps.LatLng(p.lat, p.lng));

    // ✅ 폴리라인 생성 및 지도에 표시
    const polyline = new kakao.maps.Polyline({
        path: path,
        strokeWeight: 4,
        strokeColor: color,
        strokeOpacity: 0.8,
        strokeStyle: 'solid'
    });
    polyline.setMap(map);
    currentPolylines.push(polyline);

    // ✅ 지도 범위 자동 조정
    const bounds = new kakao.maps.LatLngBounds();
    path.forEach(point => bounds.extend(point));
    map.setBounds(bounds);
}
