// WebSocket 연결 설정
const socket = new WebSocket("wss://localhost:8081/ws/dashboard");

socket.onopen = () => {
    console.log("✅ WebSocket 연결 성공!");
    // 연결이 되면 서버로부터 데이터를 요청
    socket.send("getDashboardData");
};


socket.onmessage = function(event) {
    const data = JSON.parse(event.data);

    if (data.type === "redisStats") {
        const usedMemory = Number(data.data.usedMemory) || 0;
        const connectedClients = Number(data.data.connectedClients) || 0;

        // 현재 시간 라벨 추가
        const timeLabel = new Date().toLocaleTimeString();

        // 메모리 차트 업데이트
        memoryChart.data.labels.push(timeLabel);
        memoryChart.data.datasets[0].data.push(usedMemory);
        if (memoryChart.data.labels.length > 20) memoryChart.data.labels.shift();
        if (memoryChart.data.datasets[0].data.length > 20) memoryChart.data.datasets[0].data.shift();
        memoryChart.update();

        // 클라이언트 차트 업데이트
        clientChart.data.labels.push(timeLabel);
        clientChart.data.datasets[0].data.push(connectedClients);
        if (clientChart.data.labels.length > 20) clientChart.data.labels.shift();
        if (clientChart.data.datasets[0].data.length > 20) clientChart.data.datasets[0].data.shift();
        clientChart.update();
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
