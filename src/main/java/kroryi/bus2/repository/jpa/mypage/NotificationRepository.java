package kroryi.bus2.repository.jpa.mypage;

import kroryi.bus2.entity.mypage.Notification;
import kroryi.bus2.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /* 단건·페이징 조회 */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndReadFalse(User user);
    Page<Notification> findByUser_UserId(String userId, Pageable pageable);

    /* 단건·전체 삭제 */
    void deleteByUser(User user);

    /** ✅ userId 기준 알림 전체 삭제 (JPQL 벌크) */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
        DELETE FROM Notification n
         WHERE n.user.userId = :userId
    """)
    int deleteAllByUserId(@Param("userId") String userId);
}
