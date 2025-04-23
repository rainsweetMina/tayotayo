package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.lost.LostFoundMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LostFoundMatchRepository extends JpaRepository<LostFoundMatch, Long> {
    // 매칭된 건을 카운트하는 메서드
    long countByMatchedAtIsNotNull();  // matchedAt을 기준으로 매칭된 건수 세기
    long countByMatchedByNotNull();   // matchedBy을 기준으로 매칭된 건수 세기
    boolean existsByFoundItemIdAndLostItemId(Long foundItemId, Long lostItemId);
    boolean existsByLostItemId(Long lostItemId);
    boolean existsByFoundItemId(Long foundItemId);



}

