package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.ad.Ad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AdRepository extends JpaRepository<Ad, Long> {

    Optional<Ad> findFirstByDeletedFalseAndStartDateTimeBeforeAndEndDateTimeAfterOrderByStartDateTimeDesc(
            LocalDateTime now, LocalDateTime now2);

    // 특정 기간에 생성된 광고 개수 조회 (startDateTime 필드 사용)
    long countByStartDateTimeBetween(LocalDateTime start, LocalDateTime end);

}