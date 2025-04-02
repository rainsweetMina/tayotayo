// let currentBusMarkers = [];
//
// /**
//  * 실시간 버스 위치 데이터를 받아 지도에 마커로 표시
//  * @param {Array} busDataList - 실시간 버스 위치 DTO 배열
//  */
// function drawRealtimeBusMarkers(busDataList) {
//     const map = window.kakaoMap;
//     const mapContainer = document.getElementById('map');
//
//     if (!map || !mapContainer) {
//         console.error("🛑 지도(map) 또는 mapContainer를 찾을 수 없습니다.");
//         return;
//     }
//
//     if (!Array.isArray(busDataList) || busDataList.length === 0) {
//         console.warn("⚠️ 실시간 버스 데이터가 비어있습니다:", busDataList);
//         return;
//     }
//
//     // 기존 마커 제거
//     currentBusMarkers.forEach(marker => marker.setMap(null));
//     currentBusMarkers = [];
//
//     const bounds = new kakao.maps.LatLngBounds();
//
//     busDataList.forEach(bus => {
//         const position = new kakao.maps.LatLng(bus.ypos, bus.xpos);
//         bounds.extend(position);
//
//         const marker = new kakao.maps.Marker({
//             position: position,
//             map: map,
//             title: `🚍 ${bus.routeNo}`,
//             image: new kakao.maps.MarkerImage(
//                 "/img/bus-icon.png",  // 커스텀 마커 이미지 (원하면 삭제 가능)
//                 new kakao.maps.Size(30, 30)
//             )
//         });
//
//         const infowindow = new kakao.maps.InfoWindow({
//             content: `
//                 <div style="padding:5px; font-size:13px;">
//                     🚌 노선: ${bus.routeNo}<br/>
//                     🔄 방향: ${bus.moveDir === 1 ? '정방향' : '역방향'}
//                 </div>`
//         });
//
//         kakao.maps.event.addListener(marker, 'click', () => {
//             infowindow.open(map, marker);
//         });
//
//         currentBusMarkers.push(marker);
//     });
//
//     map.setBounds(bounds); // 모든 마커를 보기 좋게 지도의 중심/줌 조정
// }

let currentBusMarkers = [];

/**
 * 실시간 버스 위치 데이터를 받아 지도에 마커로 표시
 * @param {Array} busDataList - 실시간 버스 위치 DTO 배열
 */
function drawRealtimeBusMarkers(busDataList) {
    const map = window.leafletMap;
    const mapContainer = document.getElementById('map');

    if (!map || !mapContainer) {
        console.error("🛑 지도(map) 또는 mapContainer를 찾을 수 없습니다.");
        return;
    }

    if (!Array.isArray(busDataList) || busDataList.length === 0) {
        console.warn("⚠️ 실시간 버스 데이터가 비어있습니다:", busDataList);
        return;
    }

    // 기존 마커 제거
    currentBusMarkers.forEach(marker => map.removeLayer(marker));
    currentBusMarkers = [];

    const bounds = L.latLngBounds();

    busDataList.forEach(bus => {
        const lat = parseFloat(bus.ypos);
        const lng = parseFloat(bus.xpos);

        if (!isFinite(lat) || !isFinite(lng)) return;

        const position = L.latLng(lat, lng);
        bounds.extend(position);

        const marker = L.marker(position, {
            icon: L.icon({
                iconUrl: "/img/bus-icon.png",
                iconSize: [30, 30],
                iconAnchor: [15, 30] // 이미지 아래 중앙
            }),
            title: `🚍 ${bus.routeNo}`
        });

        const popupContent = `
            <div style="padding:5px; font-size:13px;">
                🚌 노선: ${bus.routeNo}<br/>
                🔄 방향: ${bus.moveDir === 1 ? '정방향' : '역방향'}<br/>
                🚍 차량번호: ${bus.vhcNo2}
            </div>`;

        marker.bindPopup(popupContent);

        marker.addTo(map);
        currentBusMarkers.push(marker);
    });

    map.fitBounds(bounds); // 모든 마커 포함해서 보기 좋게 조정
}

