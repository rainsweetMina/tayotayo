package kroryi.bus2.repository.jpa.board;

import kroryi.bus2.entity.BusScheduleHeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusScheduleHeaderRepository extends JpaRepository<BusScheduleHeader, Integer> {
    Optional<BusScheduleHeader> findByRouteId(String routeId);
}
