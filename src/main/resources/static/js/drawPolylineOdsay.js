let currentPolylines = [];

function drawBusRouteMapOdsay(data) {
    const map = window.kakaoMap;
    const mapContainer = document.getElementById('map');

    if (!mapContainer) {
        console.error("🛑 map 요소를 찾을 수 없습니다.");
        return;
    }

    if (!data || data.length === 0) {
        console.warn("⚠️ 경로 데이터가 비어있습니다.");
        return;
    }

    // 🔥 기존 폴리라인 지우기
    currentPolylines.forEach(poly => poly.setMap(null));
    currentPolylines = [];

    // ✅ 좌표들을 LatLng 객체로 변환
    const path = data.map(p => new kakao.maps.LatLng(p.y, p.x));

    // ✅ 전체 Polyline 생성
    const polyline = new kakao.maps.Polyline({
        path: path,
        strokeWeight: 4,
        strokeColor: '#007bff',
        strokeOpacity: 0.8,
        strokeStyle: 'solid'
    });

    polyline.setMap(map);
    currentPolylines.push(polyline);

    // ✅ 지도 범위 조정
    const bounds = new kakao.maps.LatLngBounds();
    path.forEach(point => bounds.extend(point));
    map.setBounds(bounds);
}
