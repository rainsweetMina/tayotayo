package kroryi.bus2.repository.jpa.mypage;

import kroryi.bus2.dto.mypage.NotificationDTO;
import kroryi.bus2.entity.mypage.Notification;
import kroryi.bus2.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndReadFalse(User user);

    void deleteByUser(User user);

    Page<Notification> findByUser_UserId(String userId, Pageable pageable);
}
