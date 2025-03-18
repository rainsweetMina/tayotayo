package kroryi.bus2.repository;

import kroryi.bus2.entity.BusLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusLocationRepository extends JpaRepository<BusLocation, String> {
}
