package kroryi.bus2.handler;

import kroryi.bus2.service.RedisLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.ast.tree.expression.Over;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

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
@Slf4j
public class DashboardWebSocketHandler extends TextWebSocketHandler {

    private final RedisLogService redisLogService;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("📥 WebSocket 클라이언트로부터 메시지 수신: {}", message.getPayload());

        // Redis 상태 정보 수집
        Map<String, String> redisStats = redisLogService.fetchRedisStats();

        // JSON 형식으로 응답 구성
        String jsonResponse = String.format("{\"type\":\"redisStats\", \"data\":%s}", redisStats.toString());

        log.info("📡 WebSocket 클라이언트로 데이터 전송: {}", jsonResponse);


        // WebSocket 클라이언트로 전송
        session.sendMessage(new TextMessage(jsonResponse));
    }
}
