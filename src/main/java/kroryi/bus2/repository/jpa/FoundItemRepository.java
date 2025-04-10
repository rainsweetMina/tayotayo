package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.lost.FoundItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {
    List<FoundItem> findAllByVisibleTrue();

    List<FoundItem> findByVisibleTrueAndFoundTimeBefore(LocalDateTime cutoff);

    List<FoundItem> findAllByOrderByCreatedAtDesc();

}
