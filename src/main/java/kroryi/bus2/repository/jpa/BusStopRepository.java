package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.BusStop;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusStopRepository extends JpaRepository<BusStop,Long> {

    @Query("SELECT b FROM BusStop b")
    List<BusStop> findBusStops(PageRequest pageRequest);



    List<BusStop> findByBsNmContaining(String bsNm);

    @Query("SELECT b FROM BusStop b WHERE b.bsNm LIKE %:bsNm% OR REPLACE(b.bsNm, ' ', '') LIKE %:bsNm%")
    List<BusStop> searchByBsNmIgnoreSpace(@Param("bsNm") String bsNm);

}
