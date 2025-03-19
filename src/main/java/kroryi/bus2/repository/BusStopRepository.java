package kroryi.bus2.repository;

import kroryi.bus2.entity.BusStop;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface BusStopRepository extends JpaRepository<BusStop,Long> {

    @Query("SELECT b FROM BusStop b")
    List<BusStop> findBusStops(PageRequest pageRequest);

}
