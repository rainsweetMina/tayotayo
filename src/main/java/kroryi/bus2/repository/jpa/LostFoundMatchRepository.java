package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.lost.LostFoundMatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LostFoundMatchRepository extends JpaRepository<LostFoundMatch, Long> {
}
