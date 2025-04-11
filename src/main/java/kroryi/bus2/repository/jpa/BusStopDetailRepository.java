package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.busStop.BusStopInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusStopDetailRepository extends JpaRepository<BusStopInfo,Long> {
}
