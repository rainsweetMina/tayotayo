// document.addEventListener('keydown', function (event) {
//     if (event.key === "Enter" && event.target.id === "searchInput") {
//         event.preventDefault(); // 기본 동작 방지
//         // 추가적인 작업 수행
//         return searchBus(); // 선택적으로 false 반환
//     }
// });
//
// function searchBus() {
//     const query = document.getElementById('searchInput').value;
//     if (!query.trim()) {
//         alert('검색어를 입력하세요.');
//         return;
//     }
//     fetch(`/api/bus/searchBSorBN?keyword=${encodeURIComponent(query)}`, {
//         method: 'GET',
//         headers: {
//             'Content-Type': 'application/json'
//         }
//     })
//         .then(response => response.json()) // 받아온 json을 자바스크립트에서 사용가능한 js로 또 변환 (거의 필수)
//         .then(data => {
//             console.log("data2 : ", data);
//             const list = document.getElementById('busStopList');
//             list.innerHTML = '';     // 누를때마다 html를 삭제 함으로써 html가 쌓이는걸 방지
//
//             const sidebar = document.querySelector('.sidebar-sticky');
//
//             // 정류소 구분선이 있다면 삭제
//             const existingSeparator = document.getElementById('busStopSeparator');
//             if (existingSeparator) {
//                 existingSeparator.remove();
//             }
//
//             if (data.busStops.length > 0) {
//                 // 정류소 구분선 추가
//                 const separator = document.createElement('div');
//                 separator.id = 'busStopSeparator';
//                 separator.classList.add('bus-stop-separator');
//                 separator.innerHTML = `<h5>정류소</h5>`;
//                 sidebar.insertBefore(separator, list); // 정류소 리스트 위에 삽입
//             }
//
//
//             // 레스트컨트롤러에서 받은 정류장데이터(ResponseEntity.ok(list))를 데이터의 수 만큼 순회하여 html를 생성해서 화면에 출력
//             // 정류장 정보 및 클릭 이벤트 추가
//             data.busStops.forEach((busStop, index) => {
//                 const div = document.createElement('div');
//                 div.classList.add(`bus-stop${index + 1}`);
//                 div.innerHTML = `
//                         <span class="busNav" data-bsId="${busStop.bsId}" style="color: dodgerblue">
//                             <strong>${busStop.bsNm}</strong> (ID: ${busStop.bsId})<br>
//             <!--      위치: (${busStop.xpos}, ${busStop.ypos})<br>-->
//                         </span>
//                         <span class="busInfoContainer"></span>
//                     `;
//                 list.appendChild(div);
//             });
//
//             // 정류장의 태그(class = "busNav")의 span태그 요소들을 순회하며 클릭시 getBusNav()가 호출되도록 설정
//             document.querySelectorAll('.busNav').forEach(span => {
//                 span.addEventListener('click', function () {
//                     const bsId = this.getAttribute('data-bsId');
//                     const targetContainer = this.nextElementSibling;
//
//                     // 클릭한 정류소 포지션을 찾아서
//                     const busStop = data.busStops.find(stop => stop.bsId === bsId);
//                     if (!busStop) {
//                         console.error("해당 bsId에 대한 정류장 정보 없음:", bsId);
//                         return;
//                     }
//                     // 여기 넣고
//                     let Position = new kakao.maps.LatLng(busStop.ypos, busStop.xpos);
//
//
//                     /*- 이미 열려 있는 정보창이 있으면 닫고, 아니면 열기 -*/
//                     if (targetContainer.innerHTML) {
//                         targetContainer.innerHTML = ''; // 정보창이 열려 있으면 닫기
//                     } else {
//                         getBusNav(bsId, targetContainer); // 정보 불러오기
//                     }
//
//                     // 화면 이동
//                     smoothPanTo(Position);
//                     console.log("Position : " , Position)
//                 });
//             });
//
//
//             if (data.busNumbers?.length > 0) {
//                 // 제목 추가
//                 const span = document.createElement('span');
//                 span.classList.add('busRouteSpan');
//                 span.innerHTML = `<h5>버스 노선</h5>`;
//                 list.appendChild(span);
//
//                 // 구분선 추가
//                 const hr = document.createElement('hr');
//                 hr.classList.add('bus-divider');
//                 list.appendChild(hr);
//
//                 // `ul` 요소 생성
//                 const ul = document.createElement('ul');
//                 ul.classList.add('bus-list');
//
//                 data.busNumbers.forEach(bus => {
//                     const li = document.createElement('li');
//                     li.classList.add('bus-item');
//
//                     // // 노선 정보를 li의 dataset에 저장
//                     // li.dataset.routeNo = bus.routeNo;
//                     // li.dataset.routeId = bus.routeId;
//                     // li.dataset.routeNote = bus.routeNote;
//
//                     // 버스 번호 (routeNo)
//                     const mainText = document.createElement('span');
//                     mainText.textContent = bus.routeNo;
//                     mainText.style.fontWeight = 'bold'; // 또는 클래스 지정
//
//                     // 방면 정보 (routeNote)
//                     const subText = document.createElement('span');
//                     subText.textContent = ` ${bus.routeNote}`;
//                     subText.style.fontSize = '0.9em';
//                     subText.style.color = 'gray';
//                     subText.style.marginLeft = '8px';
//
//                     li.appendChild(mainText);
//                     li.appendChild(subText);
//
//                     li.addEventListener('click', () => {
//                         showLoading(); // 로딩 시작!
//                         // 첫 번째 API
//                         const stopPromise = fetch(`/api/bus/bus-route?routeId=${encodeURIComponent(bus.routeId)}`)
//                             .then(res => res.json())
//                             .then(data => {
//                                 const stopList = data.body.items;
//                                 drawBusStopMarkers(window.kakaoMap, stopList);
//                             });
//
//                         // ORS
//                         const linkPromiseORS = fetch(`/api/bus/bus-route-link?routeId=${encodeURIComponent(bus.routeId)}`)
//                             .then(res => res.json())
//                             .then(data => {
//                                 console.log(data);
//                                 drawBusRouteMapORS(data);    // ors
//                             });
//
//                         // 해당 노선의 실시간 버스 위치
//                         const linkPromiseBUS = fetch(`/api/bus/bus-route-Bus?routeId=${encodeURIComponent(bus.routeId)}`)
//                             .then(res => res.json())
//                             .then(data => {
//                                 console.log(data);
//                                 console.log("bus 좌표", data.xpos, data.ypos);
//                                 drawRealtimeBusMarkers(data);
//                             });
//
//                         // ✅ API 모두 끝난 후 로딩 숨기기
//                         Promise.all([stopPromise, linkPromiseORS, linkPromiseBUS])
//                             .then(() => hideLoading())
//                             .catch(err => {
//                                 console.error("🛑 에러 발생:", err);
//                                 hideLoading(); // 에러가 나도 로딩은 끄자!
//                             });
//                         console.log("노선번호:", bus.routeNo);
//                         console.log("노선ID:", bus.routeId);
//                         console.log("방면정보:", bus.routeNote);
//                     });
//
//                     ul.appendChild(li);
//                 });
//
//
//                 list.appendChild(ul);
//             } else {
//                 console.log("버스 노선 정보 없음.");
//             }
//
//
//             // 검색하면 검색결과로 나온 정류장의 위치 정보를 지도페이지에 전송
//             if (data.busStops && data.busStops.length > 0) {
//                 console.log("🚏 검색된 정류소 목록:");
//
//                 //  모든 정류소 데이터를 전역 변수에 배열로 저장
//                 window.selectedBusStops = data.busStops;
//
//                 //  콘솔에 모든 정류소 좌표 출력
//                 window.selectedBusStops.forEach((busStop, index) => {
//                     console.log(`  ${index + 1}. (${busStop.xpos}, ${busStop.ypos})`);
//                 });
//
//                 //  커스텀 이벤트 발생 (지도 페이지에서 감지 가능)
//                 document.dispatchEvent(new Event("busStopsUpdated"));
//             }
//
//
//         })
//         .catch(error => console.error('오류 발생:', error));
// }
//
// // 레스트컨트롤러에서 json으로 받아온 실시간 버스 도착 정보 데이터를 화면에 뿌려주는 함수
// function getBusNav(bsId, targetContainer) {
//     fetch(`/api/bus/bus-arrival?bsId=${bsId}`)
//         .then(response => response.json())
//         .then(data => {
//             console.log('버스 도착 정보 :', data);
//
//             targetContainer.innerHTML = '';  // html가 계속 쌓이는걸 방지
//
//             const body = data.body;
//
//             // totalCount가 "0"이고 msg가 존재하면 메시지 출력
//             if (body.totalCount === 0 && body.msg) {
//                 const msgDiv = document.createElement('div');
//                 msgDiv.classList.add('bus-msg');
//                 msgDiv.textContent = body.msg;
//                 targetContainer.appendChild(msgDiv);
//                 return; // 이후 코드 실행 방지
//             }
//
//             const items = body.items;
//
//
//             if (Array.isArray(items)) {
//                 items.forEach(item => {
//                     processArrList(item.routeNo, item.arrList);
//                 });
//             } else if (typeof items === 'object' && items !== null) {
//                 processArrList(items.routeNo, items.arrList);
//             }
//
//             // arrList를 처리하는 함수
//             // function processArrList(routeNo, arrList) {
//             //     if (!arrList) {
//             //         console.error("arrList가 없습니다.");
//             //         return;
//             //     }
//             //
//             //     // arrList가 객체일 수도 있으니 체크
//             //     let arrListData = arrList.arrList ? arrList.arrList : arrList;
//             //
//             //     //  배열이면 각각 처리
//             //     if (Array.isArray(arrListData)) {
//             //         arrListData.forEach(bus => {
//             //             createBusInfoElement(routeNo, bus.bsNm, bus.arrState);
//             //         });
//             //         // 객체면 바로 처리
//             //     }  else if (typeof arrListData === 'object') {
//             //         createBusInfoElement(routeNo, arrListData.bsNm, arrListData.arrState);
//             //     }
//             // }
//
//             function processArrList(routeNo, arrList) {
//                 if (!arrList) {
//                     console.error("arrList가 없습니다.");
//                     return;
//                 }
//
//                 let arrListData = arrList;
//
//                 // 객체일 경우 배열로 변환
//                 if (!Array.isArray(arrListData)) {
//                     arrListData = arrListData ? [arrListData] : [];
//                 }
//
//                 console.log("🚀 처리할 arrListData:", arrListData);
//
//                 arrListData.forEach((bus, index) => {
//                     console.log(`🟢 arrListData[${index}]:`, bus);
//                     createBusInfoElement(routeNo, bus.bsNm, bus.arrState);
//                 });
//             }
//
//
//             // HTML 요소를 생성하는 함수
//             function createBusInfoElement(routeNo, bsNm, arrState) {
//                 const span = document.createElement('span');
//                 span.classList.add('bus-info');
//                 span.innerHTML = `
//                                          <strong>노선번호:</strong> ${routeNo} <br>
//                                          <strong>정류장명:</strong> ${bsNm} <br>
//                                          <strong>도착 예정:</strong> ${arrState} <hr>
//                                           `;
//
//                 if (targetContainer.children.length === 0) {
//                     span.style.display = "block";  // 인라인 → 블록 요소로 변경
//                     span.style.marginTop = "10px";
//                 }
//
//
//                 targetContainer.appendChild(span);
//             }
//         })
//         .catch(error => console.error('오류 발생:', error));
// }
//
//
// function showLoading() {
//     const overlay = document.getElementById('loadingOverlay');
//     if (overlay) overlay.style.display = 'flex';
// }
//
// function hideLoading() {
//     const overlay = document.getElementById('loadingOverlay');
//     if (overlay) overlay.style.display = 'none';
// }


document.addEventListener('keydown', function (event) {
    if (event.key === "Enter" && event.target.id === "searchInput") {
        event.preventDefault(); // 기본 동작 방지
        // 추가적인 작업 수행
        return searchBus(); // 선택적으로 false 반환
    }
});

function searchBus() {
    const query = document.getElementById('searchInput').value;
    if (!query.trim()) {
        alert('검색어를 입력하세요.');
        return;
    }
    fetch(`/api/bus/searchBSorBN?keyword=${encodeURIComponent(query)}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json()) // 받아온 json을 자바스크립트에서 사용가능한 js로 또 변환 (거의 필수)
        .then(data => {
            console.log("data2 : ", data);
            const list = document.getElementById('busStopList');
            list.innerHTML = '';     // 누를때마다 html를 삭제 함으로써 html가 쌓이는걸 방지

            const sidebar = document.querySelector('.sidebar-sticky');

            // 정류소 구분선이 있다면 삭제
            const existingSeparator = document.getElementById('busStopSeparator');
            if (existingSeparator) {
                existingSeparator.remove();
            }

            if (data.busStops.length > 0) {
                // 정류소 구분선 추가
                const separator = document.createElement('div');
                separator.id = 'busStopSeparator';
                separator.classList.add('bus-stop-separator');
                separator.innerHTML = `<h5>정류소</h5>`;
                sidebar.insertBefore(separator, list); // 정류소 리스트 위에 삽입

                drawBusStopMarkers(window.leafletMap, data.busStops);

                // ✅ 첫 번째 정류소 위치로 지도 이동
                const first = data.busStops[0];
                if (first && first.ypos && first.xpos) {
                    const firstLatLng = L.latLng(parseFloat(first.ypos), parseFloat(first.xpos));
                    window.leafletMap.setView(firstLatLng, 16); // 줌레벨 16 정도가 적당
                }

            }


            // 레스트컨트롤러에서 받은 정류장데이터(ResponseEntity.ok(list))를 데이터의 수 만큼 순회하여 html를 생성해서 화면에 출력
            // 정류장 정보 및 클릭 이벤트 추가
            data.busStops.forEach((busStop, index) => {
                const div = document.createElement('div');
                div.classList.add(`bus-stop${index + 1}`);
                div.innerHTML = `
                        <span class="busNav" data-bsId="${busStop.bsId}" style="color: dodgerblue">
                            <strong>${busStop.bsNm}</strong> (ID: ${busStop.bsId})<br>
            <!--      위치: (${busStop.xpos}, ${busStop.ypos})<br>-->
                        </span>
                        <span class="busInfoContainer"></span>
                    `;
                list.appendChild(div);
            });

            // 정류장의 태그(class = "busNav")의 span태그 요소들을 순회하며 클릭시 getBusNav()가 호출되도록 설정
            // 전역에 선택된 정류장 마커 저장용 변수 추가
            if (!window.selectedBusStopMarker) {
                window.selectedBusStopMarker = null;
            }

            document.querySelectorAll('.busNav').forEach(span => {
                span.addEventListener('click', function () {
                    const bsId = this.getAttribute('data-bsId');
                    const targetContainer = this.nextElementSibling;

                    const busStop = data.busStops.find(stop => stop.bsId === bsId);
                    if (!busStop) {
                        console.error("해당 bsId에 대한 정류장 정보 없음:", bsId);
                        return;
                    }

                    const Position = L.latLng(busStop.ypos, busStop.xpos);

                    // ✅ 기존에 찍혀있는 단일 정류장 마커 제거
                    if (window.selectedBusStopMarker) {
                        window.leafletMap.removeLayer(window.selectedBusStopMarker);
                    }

                    // ✅ 화면 이동 추가 (줌 레벨은 원하는 대로 조정 가능)
                    window.leafletMap.setView(Position, 16);

                    // ✅ 정보창 toggle
                    if (targetContainer.innerHTML) {
                        targetContainer.innerHTML = '';
                    } else {
                        getBusNav(bsId, targetContainer);
                    }

                    console.log("📍 선택된 정류장 위치:", Position);
                });
            });



            if (data.busNumbers?.length > 0) {
                // 제목 추가
                const span = document.createElement('span');
                span.classList.add('busRouteSpan');
                span.innerHTML = `<h5>버스 노선</h5>`;
                list.appendChild(span);

                // 구분선 추가
                const hr = document.createElement('hr');
                hr.classList.add('bus-divider');
                list.appendChild(hr);

                // `ul` 요소 생성
                const ul = document.createElement('ul');
                ul.classList.add('bus-list');

                // 기존의 버스 노선 서칭
                data.busNumbers.forEach(bus => {
                    const li = document.createElement('li');
                    li.classList.add('bus-item');

                    // // 노선 정보를 li의 dataset에 저장
                    // li.dataset.routeNo = bus.routeNo;
                    // li.dataset.routeId = bus.routeId;
                    // li.dataset.routeNote = bus.routeNote;

                    // 버스 번호 (routeNo)
                    const mainText = document.createElement('span');
                    mainText.textContent = bus.routeNo;
                    mainText.style.fontWeight = 'bold'; // 또는 클래스 지정

                    // 방면 정보 (routeNote)
                    const subText = document.createElement('span');
                    subText.textContent = ` ${bus.routeNote}`;
                    subText.style.fontSize = '0.9em';
                    subText.style.color = 'gray';
                    subText.style.marginLeft = '8px';

                    li.appendChild(mainText);
                    li.appendChild(subText);

                    li.addEventListener('click', () => {
                        showLoading(); // 로딩 시작!
                        // 첫 번째 API
                        const stopPromise = fetch(`/api/bus/bus-route?routeId=${encodeURIComponent(bus.routeId)}`)
                            .then(res => res.json())
                            .then(data => {
                                // const stopList = data.body.items;
                                console.log("해당 노선의 정류장 데이터 : ", data);
                                drawRouteBusStopMarkers(window.leafletMap, data);
                            });

                        // ORS
                        const linkPromiseORS = fetch(`/api/bus/bus-route-link?routeId=${encodeURIComponent(bus.routeId)}`)
                            .then(res => res.json())
                            .then(data => {
                                console.log("해당 노선의 경로 좌표 정보 : ",data);
                                drawBusRouteMapORS(data);    // ors
                            });

                        // 해당 노선의 실시간 버스 위치
                        const linkPromiseBUS = fetch(`/api/bus/bus-route-Bus?routeId=${encodeURIComponent(bus.routeId)}`)
                            .then(res => res.json())
                            .then(data => {
                                console.log("해당 노선의 실시간 버스 정보 : ",data);
                                console.log("bus 좌표", data.xpos, data.ypos);
                                drawRealtimeBusMarkers(data);
                            });

                        // ✅ API 모두 끝난 후 로딩 숨기기
                        Promise.all([stopPromise, linkPromiseORS, linkPromiseBUS])
                            .then(() => hideLoading())
                            .catch(err => {
                                console.error("🛑 에러 발생:", err);
                                hideLoading(); // 에러가 나도 로딩은 끄자!
                            });
                        console.log("노선번호:", bus.routeNo);
                        console.log("노선ID:", bus.routeId);
                        console.log("방면정보:", bus.routeNote);
                    });

                    ul.appendChild(li);
                });

                // // 커스텀 버스 노선 서칭
                // data.CustomBusNumber.forEach(bus => {
                //     const li = document.createElement('li');
                //     li.classList.add('bus-item');
                //
                //     // 버스 번호 (routeNo)
                //     const mainText = document.createElement('span');
                //     mainText.textContent = bus.routeNo;
                //     mainText.style.fontWeight = 'bold'; // 또는 클래스 지정
                //
                //     // 방면 정보 (routeNote)
                //     const subText = document.createElement('span');
                //     subText.textContent = ` ${bus.routeNote}`;
                //     subText.style.fontSize = '0.9em';
                //     subText.style.color = 'gray';
                //     subText.style.marginLeft = '8px';
                //
                //     li.appendChild(mainText);
                //     li.appendChild(subText);
                //
                //     li.addEventListener('click', () => {
                //         showLoading(); // 로딩 시작!
                //         // 첫 번째 API
                //         const stopPromise = fetch(`/api/bus/bus-route-Custom?routeId=${encodeURIComponent(bus.routeId)}`)
                //             .then(res => res.json())
                //             .then(data => {
                //
                //                 console.log("해당 노선의 정류장 데이터 : ", data);
                //                 drawRouteBusStopMarkers(window.leafletMap, data);
                //             });
                //
                //         // ORS
                //         const linkPromiseORS = fetch(`/api/bus/bus-route-link-Custom?routeId=${encodeURIComponent(bus.routeId)}`)
                //             .then(res => res.json())
                //             .then(data => {
                //                 console.log("해당 노선의 경로 좌표 정보 : ",data);
                //                 drawBusRouteMapORS(data);    // ors
                //             });
                //
                //         // // 해당 노선의 실시간 버스 위치
                //         // const linkPromiseBUS = fetch(`/api/bus/bus-route-Bus?routeId=${encodeURIComponent(bus.routeId)}`)
                //         //     .then(res => res.json())
                //         //     .then(data => {
                //         //         console.log("해당 노선의 실시간 버스 정보 : ",data);
                //         //         console.log("bus 좌표", data.xpos, data.ypos);
                //         //         // drawRealtimeBusMarkers(data);
                //         //     });
                //
                //         // ✅ API 모두 끝난 후 로딩 숨기기
                //         Promise.all([stopPromise, linkPromiseORS])
                //             .then(() => hideLoading())
                //             .catch(err => {
                //                 console.error("🛑 에러 발생:", err);
                //                 hideLoading(); // 에러가 나도 로딩은 끄자!
                //             });
                //         console.log("노선번호:", bus.routeNo);
                //         console.log("노선ID:", bus.routeId);
                //         console.log("방면정보:", bus.routeNote);
                //     });
                //
                //     ul.appendChild(li);
                // });


                list.appendChild(ul);
            } else {
                console.log("버스 노선 정보 없음.");
            }


            // 검색하면 검색결과로 나온 정류장의 위치 정보를 지도페이지에 전송
            if (data.busStops && data.busStops.length > 0) {
                console.log("🚏 검색된 정류소 목록:");

                //  모든 정류소 데이터를 전역 변수에 배열로 저장
                window.selectedBusStops = data.busStops;

                //  콘솔에 모든 정류소 좌표 출력
                window.selectedBusStops.forEach((busStop, index) => {
                    console.log(`  ${index + 1}. (${busStop.xpos}, ${busStop.ypos})`);
                });

                //  커스텀 이벤트 발생 (지도 페이지에서 감지 가능)
                document.dispatchEvent(new Event("busStopsUpdated"));
            }


        })
        .catch(error => {
            console.error('오류 발생:', error);
        })
        .finally(() => {
            // 검색 완료 시 로딩 숨기기
            if (window.hideGlobalLoading) {
                window.hideGlobalLoading();
            }
        });
}










// 레스트컨트롤러에서 json으로 받아온 실시간 버스 도착 정보 데이터를 화면에 뿌려주는 함수
function getBusNav(bsId, targetContainer) {
    fetch(`/api/bus/bus-arrival?bsId=${bsId}`)
        .then(response => response.json())
        .then(data => {
            console.log('버스 도착 정보 :', data);

            targetContainer.innerHTML = '';  // html가 계속 쌓이는걸 방지

            const body = data.body;

            // totalCount가 "0"이고 msg가 존재하면 메시지 출력
            if (body.totalCount === 0 && body.msg) {
                const msgDiv = document.createElement('div');
                msgDiv.classList.add('bus-msg');
                msgDiv.textContent = body.msg;
                targetContainer.appendChild(msgDiv);
                return; // 이후 코드 실행 방지
            }

            const items = body.items;


            if (Array.isArray(items)) {
                items.forEach(item => {
                    processArrList(item.routeNo, item.arrList);
                });
            } else if (typeof items === 'object' && items !== null) {
                processArrList(items.routeNo, items.arrList);
            }

            function processArrList(routeNo, arrList) {
                if (!arrList) {
                    console.error("arrList가 없습니다.");
                    return;
                }

                let arrListData = arrList;

                // 객체일 경우 배열로 변환
                if (!Array.isArray(arrListData)) {
                    arrListData = arrListData ? [arrListData] : [];
                }

                console.log("🚀 처리할 arrListData:", arrListData);

                arrListData.forEach((bus, index) => {
                    console.log(`🟢 arrListData[${index}]:`, bus);
                    createBusInfoElement(routeNo, bus.bsNm, bus.arrState);
                });
            }


            // HTML 요소를 생성하는 함수
            function createBusInfoElement(routeNo, bsNm, arrState) {
                const span = document.createElement('span');
                span.classList.add('bus-info');
                span.innerHTML = `
                                         <strong>노선번호:</strong> ${routeNo} <br>
                                         <strong>정류장명:</strong> ${bsNm} <br>
                                         <strong>도착 예정:</strong> ${arrState} <hr>
                                          `;

                if (targetContainer.children.length === 0) {
                    span.style.display = "block";  // 인라인 → 블록 요소로 변경
                    span.style.marginTop = "10px";
                }


                targetContainer.appendChild(span);
            }
        })
        .catch(error => console.error('오류 발생:', error));
}








function showLoading() {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) overlay.style.display = 'flex';
}

function hideLoading() {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) overlay.style.display = 'none';
}
