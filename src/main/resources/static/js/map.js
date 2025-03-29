
kakao.maps.load(function () {
    // 지도 생성
    let mapContainer = document.getElementById('map'),
        mapOption = {
            center: new kakao.maps.LatLng(35.8668, 128.5940), // 지도의 중심좌표
            level: 3 // 지도의 확대 레벨
        };

    let map = new kakao.maps.Map(mapContainer, mapOption);
    // 전역 접근을 위해 map 객체를 window.kakaoMap에 저장
    window.kakaoMap = map;

    kakaoMap.setCopyrightPosition(kakao.maps.CopyrightPosition.BOTTOMRIGHT, true);

    // 지도 컨트롤 추가
    let mapTypeControl = new kakao.maps.MapTypeControl();
    map.addControl(mapTypeControl, kakao.maps.ControlPosition.TOPRIGHT);

    let zoomControl = new kakao.maps.ZoomControl();
    map.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);

    map.addOverlayMapTypeId(kakao.maps.MapTypeId.TRAFFIC);


    // 현재 위치로 이동
    document.getElementById('currentLocationBtn').addEventListener('click', function () {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function (position) {
                let lat = position.coords.latitude;
                let lon = position.coords.longitude;
                let moveLatLon = new kakao.maps.LatLng(lat, lon);
                kakaoMap.setCenter(moveLatLon);

                // 현재 위치에 마커 추가
                new kakao.maps.Marker({
                    position: moveLatLon,
                    map: kakaoMap
                });

            }, function () {
                console.error('위치를 가져올 수 없습니다. 위치 권한을 허용했는지 확인해주세요.');
            });
        } else {
            console.error('브라우저에서 위치 정보를 지원하지 않습니다.');
        }
    });
});

// copyRight 우측
kakaoMap.setCopyrightPosition(kakao.maps.CopyrightPosition.BOTTOMRIGHT, true);

// 전역 함수로 정의
function panTo() {
    let moveLatLon = new kakao.maps.LatLng(35.8668, 128.5940);
    kakaoMap.panTo(moveLatLon);
}

function resizeMap() {
    let mapContainer = document.getElementById('map');
    mapContainer.style.width = '650px';
    mapContainer.style.height = '650px';
}

function relayout() {
    // 전역 kakaoMap 변수를 참조하여 relayout 호출
    window.kakaoMap.relayout();
}

// 현재 넣은기능
// 1. 지도에 컨트롤러 올리기
// 2. 중심위치 이동
// 3. 현재위치
// 4. 교통 혼잡도
// 5. 지도 크기보기

// 추가 찾아볼기능(떠오르는)
//     지도위치 움직이면 가장 가까운 정류장 표시(가능할지? 찾아봤지만 우선 실패)


// 검색하면 검색된 정류장에 마커가 찎히고 첫번째로 나온 정류소로 화면 이동
document.addEventListener("busStopsUpdated", function () {
    if (window.selectedBusStops && window.selectedBusStops.length > 0) {
        console.log("🗺️ 지도페이지 - 검색된 정류소 마커 표시 시작!");

        //  기존 마커 지우기
        if (window.busStopMarkers) {
            window.busStopMarkers.forEach(marker => marker.setMap(null));
        }
        window.busStopMarkers = [];

        // 첫 번째 정류소 위치 가져오기
        let firstBusStop = window.selectedBusStops[0];
        let firstPosition = new kakao.maps.LatLng(firstBusStop.ypos, firstBusStop.xpos);

        //  검색된 모든 정류소 위치에 마커 추가
        window.selectedBusStops.forEach(busStop => {
            let markerPosition = new kakao.maps.LatLng(busStop.ypos, busStop.xpos);
            let marker = new kakao.maps.Marker({
                position: markerPosition,
                map: kakaoMap
            });

            //  생성된 마커 배열에 저장 (필요하면 나중에 지울 수 있음)
            window.busStopMarkers.push(marker);
        });

        // 첫 번째 정류소로 지도 이동
        smoothPanTo(firstPosition);

        console.log("✅ 마커 추가 완료!");
    }
});

function smoothPanTo(targetPosition) {
    let moveSpeed = 0.05; // 이동 속도 조절 (0.01 ~ 0.1 사이로 조정 가능)

    let currentCenter = kakaoMap.getCenter();
    let targetLat = targetPosition.getLat();
    let targetLng = targetPosition.getLng();
    let currentLat = currentCenter.getLat();
    let currentLng = currentCenter.getLng();

    let deltaLat = (targetLat - currentLat) * moveSpeed;
    let deltaLng = (targetLng - currentLng) * moveSpeed;

    function animateMove() {
        currentLat += deltaLat;
        currentLng += deltaLng;

        let newCenter = new kakao.maps.LatLng(currentLat, currentLng);
        kakaoMap.setCenter(newCenter);

        let distance = Math.sqrt(Math.pow(targetLat - currentLat, 2) + Math.pow(targetLng - currentLng, 2));

        if (distance > 0.0001) { // 목표 지점과의 거리가 충분하면 계속 이동
            setTimeout(animateMove, 20); // 20ms마다 이동 (속도 조절 가능)
        }
    }

    animateMove();
}
