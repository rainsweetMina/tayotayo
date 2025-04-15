package kroryi.bus2.repository.jpa.bus_stop;

import kroryi.bus2.entity.BusStopInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusStopInfoRepository extends JpaRepository<BusStopInfo, Integer> {

    BusStopInfo findByBsId(String bsId);

    void deleteByBsId(String bsId);
}
