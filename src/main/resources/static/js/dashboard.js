// WebSocket 연결 설정
let stompClient = null;

function connectWebSocket() {
    // 절대 경로로 WebSocket 연결 (프론트엔드와 백엔드가 다른 도메인/포트)
    const wsUrl = 'https://docs.yi.or.kr:8094/ws';
    const socket = new SockJS(wsUrl);
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, 
        function(frame) {  // 연결 성공 시
            console.log('✅ WebSocket 연결 성공:', frame);
            
            // 연결 상태 표시
            updateConnectionStatus('connected');
            
            // Redis 메모리 정보 구독
            stompClient.subscribe('/topic/redis-memory', function(message) {
                try {
                    const data = JSON.parse(message.body);
                    console.log('Redis 메모리 정보 수신:', data);
                    
                    // 대시보드 업데이트
                    document.getElementById('memoryUsage').innerText = data.usedMemory.toFixed(2) + ' MB';
                    document.getElementById('connectedClients').innerText = data.connectedClients || '-';
                    document.getElementById('routesCount').innerText = data.routesCount || '-';
                    document.getElementById('requestToday').innerText = data.requestToday || '-';
                } catch (error) {
                    console.error('메시지 처리 중 오류:', error);
                }
            });
        },
        function(error) {  // 연결 실패 시
            console.error('❌ WebSocket 연결 실패:', error);
            
            // 연결 상태 표시
            updateConnectionStatus('disconnected');
            
            // 에러 타입에 따른 처리
            if (error.includes('403') || error.includes('Forbidden')) {
                console.warn('🔒 권한 없음 - WebSocket 연결이 차단되었습니다.');
            } else if (error.includes('CORS')) {
                console.warn('🌐 CORS 정책 위반 - 도메인 간 접근이 차단되었습니다.');
            } else {
                console.warn('🔌 연결 실패 - 5초 후 재시도합니다.');
                // 5초 후 재연결 시도
                setTimeout(connectWebSocket, 5000);
            }
        }
    );

    // 연결이 끊어졌을 때 재연결 시도
    stompClient.ws.onclose = function() {
        console.log('🔌 WebSocket 연결 종료, 재연결 시도...');
        updateConnectionStatus('disconnected');
        setTimeout(connectWebSocket, 5000);
    };
}

// 초기 연결 시도
connectWebSocket();

// 페이지 언로드 시 연결 종료
window.onbeforeunload = function() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
};

// 대시보드 업데이트 함수
function updateDashboard(data) {
    if (data.routesCount !== undefined) {
        document.getElementById('routesCount').innerText = data.routesCount;
    }
    if (data.requestToday !== undefined) {
        document.getElementById('requestToday').innerText = data.requestToday;
    }
    if (data.memoryUsage !== undefined) {
        document.getElementById('memoryUsage').innerText = data.memoryUsage + ' MB';
    }
    if (data.connectedClients !== undefined) {
        document.getElementById('connectedClients').innerText = data.connectedClients;
    }
}

// WebSocket 연결 상태 표시 함수
function updateConnectionStatus(status) {
    const statusElement = document.getElementById('ws-status');
    if (statusElement) {
        if (status === 'connected') {
            statusElement.textContent = '🟢 연결됨';
            statusElement.className = 'text-success';
        } else {
            statusElement.textContent = '🔴 연결 끊김';
            statusElement.className = 'text-danger';
        }
    }
}

// 응답 속도 차트 렌더링
// 1. 24시간 고정 라벨 생성
const fixedLabels = Array.from({ length: 24 }, (_, i) =>
    `${String(i).padStart(2, '0')}:00`
);

// 2. 응답 속도 데이터 불러오기 및 정렬
fetch('/api/admin/metrics/response-time/hourly')
    .then(res => {
        if (!res.ok) {
            return res.text().then(text => {
                throw new Error(`서버 오류: ${res.status} - ${text}`);
            });
        }
        return res.json();
    })
    .then(data => {
      const labels = data.map(d => d.date);
      const values = data.map(d => d.averageResponseTime);

      drawChart(labels, values);
    })
    .catch(err => {
        console.error("응답속도 차트 로딩 실패", err.message);
    });

// 5. Chart.js 그리기 함수 정의
function drawChart(labels, values) {
    const ctx = document.getElementById('apiResponseChart').getContext('2d');
    const chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: '응답 속도 (ms)',
                data: [10, 30, 100, 40],
                backgroundColor: 'rgba(0, 153, 255, 0.2)',
                borderColor: '#007bff',
                borderWidth: 2,
                pointBackgroundColor: '#007bff',
                pointRadius: 4,
                tension: 0.3
            }]
        },
        options: {
            responsive: false,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: {
                        color: '#333',
                        font: { size: 13 }
                    }
                },
                title: {
                    display: true,
                    text: 'API 응답 속도 추이',
                    color: '#222',
                    font: { size: 16 }
                }
            },
            scales: {
                x: {
                    ticks: { color: '#444' },
                    grid: { color: '#ddd' }
                },
                y: {
                    beginAtZero: true,
                    ticks: { color: '#444' },
                    grid: { color: '#eee' },
                    title: {
                        display: true,
                        text: 'ms',
                        color: '#666'
                    }
                }
            }
        }
    });
}
