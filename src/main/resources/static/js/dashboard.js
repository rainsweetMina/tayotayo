// WebSocket 연결 설정
const socket = new WebSocket("wss://localhost:8081/ws/dashboard");

socket.onopen = () => {
    console.log("✅ WebSocket 연결 성공!");
    // 연결이 되면 서버로부터 데이터를 요청
    socket.send("getDashboardData");
};


socket.onmessage = function (event) {
    const response = JSON.parse(event.data);
    console.log("Received:", response);

    if (response.type === "redisStats") {
        const data = response.data;

        // 업데이트: 모든 데이터 표시
        document.getElementById("routesCount").innerText = data.routesCount || "-";
        document.getElementById("requestToday").innerText = data.requestToday || "-";
        document.getElementById("memoryUsage").innerText = data.usedMemory || "-";
        document.getElementById("connectedClients").innerText = data.connectedClients || "-";
    }
};

socket.onerror = (error) => {
    console.error("❌ WebSocket 오류:", error);
};

socket.onclose = () => {
    console.log("🔌 WebSocket 연결 종료");
};

// 대시보드 업데이트 함수
function updateDashboard(data) {
    if (data.routesCount !== undefined) {
        document.getElementById("routesCount").innerText = data.routesCount;
    }
    if (data.requestToday !== undefined) {
        document.getElementById("requestToday").innerText = data.requestToday;
    }
    if (data.memoryUsage !== undefined) {
        document.getElementById("memoryUsage").innerText = data.memoryUsage + " MB";
    }
    if (data.connectedClients !== undefined) {
        document.getElementById("connectedClients").innerText = data.connectedClients;
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