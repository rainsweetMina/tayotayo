package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.lost.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    // ✅ 마이페이지 조회용 - 본인의 글 중 삭제되지 않은 것만
    List<LostItem> findAllByReporterIdAndDeletedFalse(Long reporterId);


    @Query("SELECT l FROM LostItem l WHERE " +
            "(:itemName IS NULL OR l.title LIKE CONCAT('%', :itemName, '%')) AND " +
            "(:busCompany IS NULL OR l.busCompany LIKE CONCAT('%', :busCompany, '%')) AND " +  // 🔁 수정
            "(:busNumber IS NULL OR l.busNumber LIKE CONCAT('%', :busNumber, '%')) AND " +    // 🔁 수정
            "(:startDate IS NULL OR l.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR l.createdAt <= :endDate) AND " +
            "l.visible = true AND l.deleted = false")
    List<LostItem> searchByConditions(
            @Param("itemName") String itemName,
            @Param("busCompany") String busCompany,
            @Param("busNumber") String busNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


}

