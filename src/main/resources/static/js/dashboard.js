// WebSocket 연결 설정
const socket = new WebSocket("wss://localhost:8081/ws/dashboard");

socket.onopen = () => {
    console.log("✅ WebSocket 연결 성공!");
    // 연결이 되면 서버로부터 데이터를 요청
    socket.send("getDashboardData");
};


socket.onmessage = function (event) {
    console.log("📥 받은 데이터: ", event.data);

    try {
        const data = JSON.parse(event.data);
        if (data.type === "redisStats") {
            updateDashboard(data.data); // 데이터를 HTML로 반영하는 함수
        }
    } catch (e) {
        console.error("❌ JSON 파싱 에러", e);
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
