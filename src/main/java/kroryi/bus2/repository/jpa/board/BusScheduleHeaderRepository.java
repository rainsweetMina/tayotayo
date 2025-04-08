package kroryi.bus2.repository.jpa.board;

import kroryi.bus2.entity.BusScheduleHeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusScheduleHeaderRepository extends JpaRepository<BusScheduleHeader, Integer> {
    List<BusScheduleHeader> findByRouteId(String routeId);

    Optional<BusScheduleHeader> findByRouteIdAndMoveDir(String routeId, String moveDir);


}
