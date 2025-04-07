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

