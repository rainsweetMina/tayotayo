package kroryi.bus2.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import kroryi.bus2.service.admin.RedisLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

/*
    Handler 패키지 따로 만든 이유
      역할 분리: WebSocket 핸들러는 컨트롤러와 성격이 다름.
      컨트롤러: HTTP 요청을 처리.
      핸들러: WebSocket 연결을 관리.
      유지보수성: 다른 핸들러가 추가되어도 패키지 구조가 명확해서 관리하기 쉬움.
*/

@Component
@Log4j2
@RequiredArgsConstructor
public class DashboardWebSocketHandler extends TextWebSocketHandler {

    private final RedisLogService redisLogService;
    private final ObjectMapper objectMapper;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("📥 WebSocket 클라이언트로부터 메시지 수신: {}", message.getPayload());

        // Redis 상태 정보 수집
        var redisStats = redisLogService.getRedisInfo();

        // Redis 정보를 Map으로 변환
        Map<String, Object> redisData = new HashMap<>();
        if (redisStats.getError() == null) {
            redisData.put("usedMemory", redisStats.getUsedMemory());
            redisData.put("maxMemory", redisStats.getMaxMemory());
            redisData.put("connectedClients", redisStats.getConnectedClients());
            redisData.put("uptime", redisStats.getUptime());
            redisData.put("version", redisStats.getVersion());
        } else {
            redisData.put("error", redisStats.getError());
        }

        // JSON 형식으로 응답 구성
        String jsonResponse = objectMapper.writeValueAsString(Map.of(
                "type", "redisStats",
                "data", redisData
        ));

        log.info("📡 WebSocket 클라이언트로 데이터 전송: {}", jsonResponse);

        // WebSocket 클라이언트로 전송
        session.sendMessage(new TextMessage(jsonResponse));
    }
}