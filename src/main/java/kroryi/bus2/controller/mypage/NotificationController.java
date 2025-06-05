package kroryi.bus2.controller.mypage;

import kroryi.bus2.dto.mypage.CountResponseDTO;
import kroryi.bus2.dto.mypage.NotificationDTO;
import kroryi.bus2.service.mypage.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mypage/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(@RequestParam String userId) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
    }

    @GetMapping("/count")
    public ResponseEntity<CountResponseDTO> countUnread(Authentication auth) {
        String userId = auth.getName();
        long count = notificationService.getNotificationsForUser(userId).stream()
                .filter(n -> !n.isRead())
                .count();

        return ResponseEntity.ok(new CountResponseDTO(count));
    }


    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/readAll")
    public ResponseEntity<Void> markAllAsRead(@RequestParam String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll(@RequestParam String userId) {
        notificationService.deleteAll(userId);
        return ResponseEntity.noContent().build();
    }
}
