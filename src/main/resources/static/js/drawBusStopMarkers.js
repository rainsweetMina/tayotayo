// function drawBusStopMarkers(map, stopData) {
//
//     // 1. 기존 마커 제거
//     window.busStopMarkers?.forEach(marker => marker.setMap(null));
//     window.busStopMarkers = [];
//
//     if (!Array.isArray(stopData) || stopData.length === 0) {
//         console.warn("🚧 정류장 데이터가 없습니다.");
//         return;
//     }
//
//     window.busStopMarkers = window.busStopMarkers || [];
//
//     stopData.forEach(stop => {
//         const position = new kakao.maps.LatLng(parseFloat(stop.yPos), parseFloat(stop.xPos));
//         const markerColor = stop.moveDir === "0" ? 'red' : 'blue';
//
//         const markerImage = new kakao.maps.MarkerImage(
//             '/img/bus-stop-icon.png',                 // 정류장용 아이콘 경로
//             new kakao.maps.Size(20, 20)
//         );
//
//         // 2. 마커 생성
//         const marker = new kakao.maps.Marker({
//             position: position,
//             map: null, // 처음엔 숨김
//             title: stop.bsNm,
//             image: markerImage
//         });
//
//         // 3. 전역 배열에 저장해야 나중에 보이고/숨기고 가능
//         window.busStopMarkers.push(marker);
//
//         // 4. 인포윈도우
//         const infowindow = new kakao.maps.InfoWindow({
//             content: `<div style="padding:10px;">${stop.bsNm}</div>`
//         });
//
//         kakao.maps.event.addListener(marker, 'mouseover', () => infowindow.open(map, marker));
//         kakao.maps.event.addListener(marker, 'mouseout', () => infowindow.close());
//     });
//
//     // 5. 줌 이벤트 등록 (최초 한 번만)
//     if (!window.markerZoomListenerAdded) {
//         kakao.maps.event.addListener(map, 'zoom_changed', handleZoomMarkerVisibility);
//         window.markerZoomListenerAdded = true;
//     }
//
//     // 6. 초기 상태 반영
//     handleZoomMarkerVisibility();
//
//     function handleZoomMarkerVisibility() {
//         const level = map.getLevel();
//         const showMarkers = level <= 5;
//
//         window.busStopMarkers.forEach(marker => {
//             marker.setMap(showMarkers ? map : null);
//         });
//     }
// }

function drawBusStopMarkers(map, stopData) {
    // 1. 기존 마커 제거
    window.busStopMarkers?.forEach(marker => map.removeLayer(marker));
    window.busStopMarkers = [];

    if (!Array.isArray(stopData) || stopData.length === 0) {
        console.warn("🚧 정류장 데이터가 없습니다.");
        return;
    }

    stopData.forEach(stop => {
        const lat = parseFloat(stop.yPos);
        const lng = parseFloat(stop.xPos);
        if (!isFinite(lat) || !isFinite(lng)) return;

        const marker = L.marker([lat, lng], {
            icon: L.icon({
                iconUrl: '/img/bus-stop-icon.png',
                iconSize: [20, 20],
                iconAnchor: [10, 20]
            }),
            title: stop.bsNm
        });

        marker.bindPopup(`<div style="padding:10px;">${stop.bsNm}</div><br>`);

        marker.addTo(map);
        window.busStopMarkers.push(marker);
    });

    // 5. 줌 이벤트 등록 (최초 한 번만)
    if (!window.markerZoomListenerAdded) {
        map.on('zoomend', handleZoomMarkerVisibility); // Leaflet은 zoomend 이벤트
        window.markerZoomListenerAdded = true;
    }

    // 6. 초기 상태 반영
    handleZoomMarkerVisibility();

    function handleZoomMarkerVisibility() {
        const zoom = map.getZoom();
        const showMarkers = zoom >= 13; // Leaflet은 숫자가 클수록 확대됨

        window.busStopMarkers.forEach(marker => {
            if (showMarkers) {
                marker.addTo(map);
            } else {
                map.removeLayer(marker);
            }
        });
    }
}

