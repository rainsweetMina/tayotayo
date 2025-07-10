package kroryi.bus2.config.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
@RequiredArgsConstructor
@Log4j2
public class WebSocketEventListener {

    /**
     * WebSocket 연결 성공 이벤트 처리
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        String user = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "anonymous";

        log.info("✅ WebSocket 연결 성공 - Session: {}, User: {}", sessionId, user);

        // 연결된 사용자 수 증가 등의 로직을 여기에 추가할 수 있습니다
    }

    /**
     * WebSocket 연결 해제 이벤트 처리
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        String user = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "anonymous";

        log.info("❌ WebSocket 연결 해제 - Session: {}, User: {}", sessionId, user);

        // 연결된 사용자 수 감소 등의 로직을 여기에 추가할 수 있습니다
    }

    /**
     * WebSocket 구독 이벤트 처리
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        String user = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "anonymous";

        log.info("📡 WebSocket 구독 - Session: {}, User: {}, Destination: {}",
                sessionId, user, destination);
    }

    /**
     * WebSocket 구독 해제 이벤트 처리
     */
    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        String user = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "anonymous";

        log.info("📡 WebSocket 구독 해제 - Session: {}, User: {}", sessionId, user);
    }
}