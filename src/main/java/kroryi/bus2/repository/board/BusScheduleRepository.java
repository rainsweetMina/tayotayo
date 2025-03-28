package kroryi.bus2.repository.board;

import kroryi.bus2.entity.board.BusSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusScheduleRepository extends JpaRepository<BusSchedule, Integer> {
//    List<BusSchedule> findByRouteId(String routeId);

}