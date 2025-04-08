package kroryi.bus2.repository.jpa.board;

import kroryi.bus2.entity.BusSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusScheduleRepository extends JpaRepository<BusSchedule, Long> {
    List<BusSchedule> findByRouteId(String routeId);

    List<BusSchedule> findByRouteIdAndMoveDir(String routeId, String moveDir);
}