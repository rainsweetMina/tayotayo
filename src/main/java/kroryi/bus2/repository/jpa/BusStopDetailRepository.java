package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.BusStopInfo;
import kroryi.bus2.entity.bus_stop.BusStop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusStopDetailRepository extends JpaRepository<BusStopInfo,Long> {
}
