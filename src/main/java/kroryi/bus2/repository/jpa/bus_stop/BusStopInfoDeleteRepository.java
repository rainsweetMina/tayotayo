package kroryi.bus2.repository.jpa.bus_stop;

import kroryi.bus2.entity.busStop.BusStopDelete;
import kroryi.bus2.entity.busStop.BusStopInfoDelete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusStopInfoDeleteRepository extends JpaRepository<BusStopInfoDelete,Long> {
}
