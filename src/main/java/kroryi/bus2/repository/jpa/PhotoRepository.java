package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.lost.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Photo findByFoundItemId(Long foundItemId);

}
