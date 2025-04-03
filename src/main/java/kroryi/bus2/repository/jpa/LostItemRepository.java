package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {
}

