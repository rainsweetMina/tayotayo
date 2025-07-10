package kroryi.bus2.controller;

import kroryi.bus2.service.admin.RedisLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisLogService redisLogService;

    /**
     * 클라이언트로부터 메시지를 받아서 처리
     */
    @MessageMapping("/request-redis-stats")
    @SendTo("/topic/redis-memory")
    public Map<String, Object> handleRedisStatsRequest() {
        log.info("📥 Redis 상태 정보 요청 수신");
        
        try {
            var redisStats = redisLogService.getRedisInfo();
            
            Map<String, Object> response = new HashMap<>();
            if (redisStats.getError() == null) {
                response.put("usedMemory", redisStats.getUsedMemory());
                response.put("maxMemory", redisStats.getMaxMemory());
                response.put("connectedClients", redisStats.getConnectedClients());
                response.put("uptime", redisStats.getUptime());
                response.put("version", redisStats.getVersion());
                response.put("timestamp", System.currentTimeMillis());
            } else {
                response.put("error", redisStats.getError());
            }
            
            log.info("📡 Redis 상태 정보 전송: {}", response);
            return response;
            
        } catch (Exception e) {
            log.error("❌ Redis 상태 정보 조회 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Redis 상태 정보 조회 중 오류가 발생했습니다.");
            return errorResponse;
        }
    }

    /**
     * 관리자 활동 로그 전송
     */
    @MessageMapping("/admin-activity")
    @SendTo("/topic/admin-audit-logs")
    public Map<String, Object> handleAdminActivity(Map<String, Object> activity) {
        log.info("📥 관리자 활동 로그 수신: {}", activity);
        
        // 타임스탬프 추가
        activity.put("timestamp", System.currentTimeMillis());
        
        return activity;
    }

    /**
     * 특정 사용자에게 메시지 전송
     */
    public void sendToUser(String userId, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(userId, destination, payload);
        log.info("📡 사용자 {}에게 메시지 전송: {} -> {}", userId, destination, payload);
    }

    /**
     * 모든 구독자에게 메시지 전송
     */
    public void sendToAll(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
        log.info("📡 모든 구독자에게 메시지 전송: {} -> {}", destination, payload);
    }

    /**
     * 주기적으로 Redis 상태 정보를 모든 구독자에게 전송 (5초마다)
     */
    @Scheduled(fixedRate = 5000)
    public void broadcastRedisStats() {
        try {
            var redisStats = redisLogService.getRedisInfo();
            
            Map<String, Object> stats = new HashMap<>();
            if (redisStats.getError() == null) {
                stats.put("usedMemory", redisStats.getUsedMemory());
                stats.put("maxMemory", redisStats.getMaxMemory());
                stats.put("connectedClients", redisStats.getConnectedClients());
                stats.put("uptime", redisStats.getUptime());
                stats.put("version", redisStats.getVersion());
                stats.put("timestamp", System.currentTimeMillis());
                
                sendToAll("/topic/redis-memory", stats);
            }
        } catch (Exception e) {
            log.error("❌ 주기적 Redis 상태 정보 전송 실패", e);
        }
    }

    /**
     * 시스템 알림 전송
     */
    public void sendSystemNotification(String message, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("message", message);
        notification.put("type", type); // info, warning, error
        notification.put("timestamp", System.currentTimeMillis());
        
        sendToAll("/topic/system-notifications", notification);
        log.info("📢 시스템 알림 전송: {} ({})", message, type);
    }
} 