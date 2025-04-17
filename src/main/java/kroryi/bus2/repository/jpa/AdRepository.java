package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.ad.Ad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AdRepository extends JpaRepository<Ad, Long> {

    Optional<Ad> findFirstByDeletedFalseAndStartDateTimeBeforeAndEndDateTimeAfterOrderByStartDateTimeDesc(
            LocalDateTime now, LocalDateTime now2);

}