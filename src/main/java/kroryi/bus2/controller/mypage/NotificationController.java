package kroryi.bus2.controller.mypage;

import kroryi.bus2.dto.mypage.CountResponseDTO;
import kroryi.bus2.dto.mypage.NotificationDTO;
import kroryi.bus2.service.mypage.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mypage/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** 🔍 알림 목록 (페이징) */
    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NotificationDTO> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    /** 🔔 읽지 않은 알림 건수 */
    @GetMapping("/count")
    public ResponseEntity<CountResponseDTO> countUnread(Authentication authentication) {
        String userId = authentication.getName();
        long count = notificationService.getNotificationsForUser(userId)
                .stream()
                .filter(n -> !n.isRead())
                .count();
        return ResponseEntity.ok(new CountResponseDTO(count));
    }

    /** ✅ 단일 읽음 */
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    /** ✅ 전체 읽음 */
    @PostMapping("/readAll")
    public ResponseEntity<Void> markAllAsRead(@RequestParam String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    /** 🗑️ 단일 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    /** 🗑️ 전체 삭제 */
    @DeleteMapping
    public ResponseEntity<Void> deleteAll(@RequestParam String userId) {
        notificationService.deleteAll(userId);
        return ResponseEntity.noContent().build();
    }
}
