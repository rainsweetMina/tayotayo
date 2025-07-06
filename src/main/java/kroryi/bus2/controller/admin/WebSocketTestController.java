package kroryi.bus2.controller.admin;

import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@Log4j2
public class WebSocketTestController {

    @GetMapping("/ws-test")
    @ResponseBody
    public String wsTest() {
        return "WebSocket 테스트 페이지 - 연결 상태 확인";
    }

    @MessageMapping("/test")
    @SendTo("/topic/test")
    public Map<String, Object> handleTestMessage(String message) {
        log.info("WebSocket 테스트 메시지 수신: {}", message);
        return Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "message", "서버에서 응답: " + message,
            "status", "success"
        );
    }
} 