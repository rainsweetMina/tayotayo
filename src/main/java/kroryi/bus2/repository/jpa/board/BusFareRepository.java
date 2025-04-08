package kroryi.bus2.repository.jpa.board;

import kroryi.bus2.entity.BusFare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusFareRepository extends JpaRepository<BusFare, Integer> {
}
