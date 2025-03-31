let currentPolylines = []; // 기존 선들 제거용

function drawBusRouteMapCustomFile(data) {
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

    // 🔥 기존 폴리라인 제거
    currentPolylines.forEach(poly => poly.setMap(null));
    currentPolylines = [];

    const bounds = new kakao.maps.LatLngBounds();

    data.forEach(link => {
        if (!link.coords || link.coords.length < 2) return;

        const path = link.coords.map(p => new kakao.maps.LatLng(p.ypos, p.xpos));

        const polyline = new kakao.maps.Polyline({
            path: path,
            strokeWeight: 4,
            strokeColor: '#007bff', // 필요하면 moveDir별 색상도 가능
            strokeOpacity: 0.8,
            strokeStyle: 'solid'
        });

        polyline.setMap(map);
        currentPolylines.push(polyline);

        path.forEach(p => bounds.extend(p)); // 지도 범위 조정
    });

    map.setBounds(bounds);
}
