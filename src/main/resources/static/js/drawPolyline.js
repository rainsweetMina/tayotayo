let currentPolylines  = []; // 전역 변수로 선언

function drawBusRouteMap(data) {
    const map = window.kakaoMap; // ✅ 전역 kakaoMap 사용
    const mapContainer = document.getElementById('map');

    if (!mapContainer) {
        console.error("🛑 map 요소를 찾을 수 없습니다.");
        return;
    }

    if (!data || data.length === 0) {
        console.warn("⚠️ 경로 데이터가 비어있습니다.");
        return;
    }
    //
    // const mapOption = {
    //     center: new kakao.maps.LatLng(data[0].stY, data[0].stX),
    //     level: 5
    // };
    // const map = new kakao.maps.Map(mapContainer, mapOption);

    // 🔥 기존 선들 지우기
    currentPolylines.forEach(poly => poly.setMap(null));
    currentPolylines = []; // 배열 초기화


    const bounds = new kakao.maps.LatLngBounds();

    data.forEach(link => {
        const start = new kakao.maps.LatLng(link.stY, link.stX);
        const end = new kakao.maps.LatLng(link.edY, link.edX);

        const strokeColor = link.moveDir === 0 ? '#FF0000' : '#007bff';

        const polyline = new kakao.maps.Polyline({
            path: [start, end],
            strokeWeight: 4,
            strokeColor: strokeColor,
            strokeOpacity: 0.8,
            strokeStyle: 'solid'
        });

        polyline.setMap(map);
        currentPolylines.push(polyline); // ✅ 배열에 저장

        bounds.extend(start);
        bounds.extend(end);
    });

    map.setBounds(bounds);
}
