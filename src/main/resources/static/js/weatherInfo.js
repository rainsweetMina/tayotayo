
const { nx, ny } = convertToGrid(35.860533, 128.595014);
let apiKey = "";

function convertToGrid(lat, lon) {
    const RE = 6371.00877; // 지구 반경(km)
    const GRID = 5.0;      // 격자 간격(km)
    const SLAT1 = 30.0;    // 투영 위도1
    const SLAT2 = 60.0;    // 투영 위도2
    const OLON = 126.0;    // 기준점 경도
    const OLAT = 38.0;     // 기준점 위도
    const XO = 43;         // 기준점 X좌표 (격자)
    const YO = 136;        // 기준점 Y좌표 (격자)

    const DEGRAD = Math.PI / 180.0;
    const re = RE / GRID;
    const slat1 = SLAT1 * DEGRAD;
    const slat2 = SLAT2 * DEGRAD;
    const olon = OLON * DEGRAD;
    const olat = OLAT * DEGRAD;

    let sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
    sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
    let sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
    sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
    let ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
    ro = re * sf / Math.pow(ro, sn);

    const ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
    let ry = re * sf / Math.pow(ra, sn);
    let theta = lon * DEGRAD - olon;
    if (theta > Math.PI) theta -= 2.0 * Math.PI;
    if (theta < -Math.PI) theta += 2.0 * Math.PI;
    theta *= sn;

    const x = Math.floor(ry * Math.sin(theta) + XO + 0.5);
    const y = Math.floor(ro - ry * Math.cos(theta) + YO + 0.5);
    return { nx: x, ny: y };
}


function getBaseDateTime() {
    const now = new Date();
    now.setMinutes(now.getMinutes() - 40);
    const yyyyMMdd = now.toISOString().slice(0, 10).replace(/-/g, '');
    const hour = String(now.getHours()).padStart(2, '0');
    const minute = String(now.getMinutes()).padStart(2, '0');
    return { base_date: yyyyMMdd, base_time: `${hour}${minute}` };
}

function rotateDisplay(elements, targetEl) {
    let index = 0;
    const update = () => {
        targetEl.classList.remove("visible");
        setTimeout(() => {
            targetEl.innerText = elements[index];
            targetEl.classList.add("visible");
            index = (index + 1) % elements.length;
        }, 300);
    };
    update();
    setInterval(update, 5000);
}

function fetchWeather() {
    const { base_date, base_time } = getBaseDateTime();
    const url = `https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst?serviceKey=${encodeURIComponent(apiKey)}&numOfRows=100&pageNo=1&dataType=JSON&base_date=${base_date}&base_time=${base_time}&nx=${nx}&ny=${ny}`;

    fetch(url)
        .then(res => res.json())
        .then(data => {
            console.log(data);
            const items = data?.response?.body?.items?.item;
            if (!Array.isArray(items)) throw new Error("API 응답 오류");

            const now = new Date();
            const fcstDate = now.toISOString().slice(0, 10).replace(/-/g, '');
            const fcstTime = String(now.getHours()).padStart(2, '0') + String(now.getMinutes()).padStart(2, '0');

            // 가장 가까운 시간값을 찾기
            function getVal(cat) {
                const filtered = items.filter(i => i.category === cat && i.fcstDate === fcstDate);
                const sorted = filtered.sort((a, b) => Math.abs(fcstTime - a.fcstTime) - Math.abs(fcstTime - b.fcstTime));
                return sorted[0]?.fcstValue ?? "N/A";
            }

            function windDirection(degree) {
                const dirs = ["북", "북북동", "북동", "동북동", "동", "동남동", "남동", "남남동",
                    "남", "남남서", "남서", "서남서", "서", "서북서", "북서", "북북서"];
                const idx = Math.round((degree % 360) / 22.5) % 16;
                return dirs[idx];
            }

            const temp = getVal("T1H");
            const wind = getVal("WSD");
            const sky = getVal("SKY");
            const pty = getVal("PTY");
            const rn1 = getVal("RN1");
            const vec = getVal("VEC"); // 풍향
            const windDir = vec !== "N/A" ? windDirection(Number(vec)) : "N/A";

            const skyMap = { "1": "☀️", "3": "⛅", "4": "☁️" };
            const ptyMap = {
                "0": "", // 강수없음이면 sky 상태로 대체
                "1": "🌧",
                "2": "🌨",
                "3": "❄",
                "4": "🌦"
            };

            const weatherSymbol = (pty !== "0" && pty !== "N/A") ? ptyMap[pty] : skyMap[sky] || "❓";

            const showList = (pty !== "0" && pty !== "N/A")
                ? [`${temp}℃ ${weatherSymbol}`, `${windDir}풍 ${wind}m/s ${weatherSymbol}`, `강수량: ${rn1} ${weatherSymbol}`]
                : [`${temp}℃ ${weatherSymbol}`, `${windDir}풍 ${wind}m/s ${weatherSymbol}`];

            const target = document.getElementById("weatherData");
            rotateDisplay(showList, target);
        })
        .catch(err => {
            console.error("날씨 API 오류:", err);
            document.getElementById("weatherData").innerText = "날씨 정보를 불러올 수 없습니다.";
        });
}

// API 키를 서버에서 가져오는 부분
fetch("/api/public/api-key")
    .then(res => res.json())
    .then(data => {
        apiKey = data.apiKey;
        fetchWeather();
        setInterval(fetchWeather, 1000 * 60 * 30); // 30분마다 자동 갱신
    })
    .catch(err => {
        console.error("API 키 로딩 실패:", err);
        document.getElementById("weatherData").innerText = "API 키를 불러올 수 없습니다.";
    });