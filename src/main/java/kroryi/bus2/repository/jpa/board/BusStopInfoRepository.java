package kroryi.bus2.repository.jpa.board;

import kroryi.bus2.entity.busStop.BusStopInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusStopInfoRepository extends JpaRepository<BusStopInfo,Long> {
    @Query("SELECT b, s.bsNm FROM BusStopInfo b JOIN BusStop s ON b.bsId = s.bsId")
    List<String> findAllBusStopInfo();
}
