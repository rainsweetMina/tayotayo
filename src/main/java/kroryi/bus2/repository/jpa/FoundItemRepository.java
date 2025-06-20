package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.lost.FoundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {

    // 관리자가 보는 전체 목록 (삭제되지 않은 것만)
    List<FoundItem> findByIsDeletedFalse();

    // 사용자나 외부에 공개할 목록 (숨김 및 삭제되지 않은 것만)
    List<FoundItem> findByIsDeletedFalseAndVisibleTrue();

    // ⬇️ 7일 이내만 필터링 (★ 추가!)
    @Query("""
    SELECT f FROM FoundItem f
    WHERE f.isDeleted = false
      AND f.visible = true
      AND f.createdAt >= :cutoff
    """)
    List<FoundItem> findVisibleForUserWithin7Days(@Param("cutoff") LocalDateTime cutoff);

    // 아래부터는 기존 코드 그대로!
    List<FoundItem> findByVisibleTrueAndCreatedAtBefore(LocalDateTime cutoff);

    @Query("SELECT COUNT(f) FROM FoundItem f WHERE f.status = 'RETURNED'")
    Long countMatchedIncludingManual();

    Optional<FoundItem> findByIdAndIsDeletedFalseAndVisibleTrue(Long id);

    List<FoundItem> findAllByIsHiddenFalseAndIsDeletedFalse();

    @Query("""
    SELECT f FROM FoundItem f
    WHERE f.isDeleted = false
      AND f.visible = true
      AND (:keyword IS NULL OR f.itemName LIKE %:keyword% OR f.content LIKE %:keyword%)
      AND (:busCompany IS NULL OR f.busCompany = :busCompany)
      AND (:busNumber IS NULL OR f.busNumber = :busNumber)
      AND f.createdAt BETWEEN :startDate AND :endDate
    """)
    List<FoundItem> searchFoundItems(
            @Param("keyword") String keyword,
            @Param("busCompany") String busCompany,
            @Param("busNumber") String busNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    @Query("""
SELECT f FROM FoundItem f
WHERE f.isDeleted = false
  AND (
    :keyword IS NULL
    OR LOWER(f.itemName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(f.busCompany) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(f.busNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
  )
""")
    List<FoundItem> searchByKeywordForAdmin(@Param("keyword") String keyword);

}
