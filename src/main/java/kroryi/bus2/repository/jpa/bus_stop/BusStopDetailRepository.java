package kroryi.bus2.repository.jpa.bus_stop;

import kroryi.bus2.entity.busStop.BusStopInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusStopDetailRepository extends JpaRepository<BusStopInfo,Long> {
}
