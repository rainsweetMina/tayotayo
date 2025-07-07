package kroryi.bus2.service.mypage;

import kroryi.bus2.dto.mypage.NotificationDTO;
import kroryi.bus2.entity.mypage.Notification;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.UserRepository;
import kroryi.bus2.repository.jpa.mypage.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /* ========================= 조회 ========================= */

    public Page<NotificationDTO> getUserNotifications(String userId, Pageable pageable) {
        return notificationRepository.findByUser_UserId(userId, pageable)
                .map(NotificationDTO::fromEntity);
    }

    public List<NotificationDTO> getNotificationsForUser(String userId) {
        User user = findUser(userId);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /* ========================= 생성 ========================= */

    public void createNotification(String userId, String message) {
        User user = findUser(userId);
        Notification n = Notification.builder()
                .user(user)
                .title("알림")
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(n);
    }

    /* ===================== 읽음 처리 ===================== */

    @Transactional
    public void markAsRead(Long id) {
        notificationRepository.findById(id)
                .ifPresent(n -> n.setRead(true));
    }

    @Transactional
    public void markAllAsRead(String userId) {
        User user = findUser(userId);
        List<Notification> unread = notificationRepository.findByUserAndReadFalse(user);
        unread.forEach(n -> n.setRead(true));          // flush 시점에 일괄 업데이트
    }

    /* ========================= 삭제 ========================= */

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll(String userId) {
        // JPQL 벌크 삭제로 한 번에 처리
        notificationRepository.deleteAllByUserId(userId);
    }

    /* ====================== 내부 공통 ====================== */

    private User findUser(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
    }
}
