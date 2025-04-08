package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.Ad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdRepository extends JpaRepository<Ad, Long> {
}