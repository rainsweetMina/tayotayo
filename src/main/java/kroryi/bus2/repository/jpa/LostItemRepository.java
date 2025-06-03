package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.lost.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {
    // 🔹 기존: 일반 회원 조회용 (visible = true만 조회)
    List<LostItem> findAllByVisibleTrue();

    // ✅ 추가: 관리자용 전체 조회 (숨김 포함)
    @Query("SELECT l FROM LostItem l")
    List<LostItem> findAllIncludingHidden();

    List<LostItem> findByVisibleTrueAndCreatedAtBefore(LocalDateTime cutoff);

    List<LostItem> findAllByOrderByCreatedAtDesc();

    List<LostItem> findAllByReporterId(Long reporterId);

    List<LostItem> findByDeletedFalseAndVisibleTrue();

}

