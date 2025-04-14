package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.lost.FoundItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {

    // 관리자가 보는 전체 목록 (삭제되지 않은 것만)
    List<FoundItem> findByIsDeletedFalse();


    // 사용자나 외부에 공개할 목록 (숨김 및 삭제되지 않은 것만)
    List<FoundItem> findByIsDeletedFalseAndVisibleTrue();
    List<FoundItem> findByVisibleTrueAndFoundTimeBefore(LocalDateTime cutoff);

}
