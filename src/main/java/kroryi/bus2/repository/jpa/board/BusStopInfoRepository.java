package kroryi.bus2.repository.jpa.board;

import kroryi.bus2.entity.busStop.BusStop;
import kroryi.bus2.entity.busStop.BusStopInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BusStopInfoRepository extends JpaRepository<BusStopInfo,Long> {
    /* 드롭다운에 필요한 쿼리 */
    @Query("SELECT DISTINCT i.district FROM BusStopInfo i")
    List<String> findDistinctDistrict();

    @Query("SELECT DISTINCT i.neighborhood FROM BusStopInfo i WHERE i.district = :district")
    List<String> findNeighborhoodsByDistrict(@Param("district") String district);

    @Query("SELECT b FROM BusStopInfo b JOIN FETCH b.busStop WHERE b.district = :district")
    List<BusStopInfo> findByDistrict(@Param("district") String district);

    @Query("SELECT b FROM BusStopInfo b JOIN FETCH b.busStop WHERE b.district = :district AND b.neighborhood = :neighborhood")
    List<BusStopInfo> findByDistrictAndNeighborhood(@Param("district") String district, @Param("neighborhood") String neighborhood);

    @Query("SELECT b FROM BusStopInfo b JOIN FETCH b.busStop JOIN RouteStopLink r ON r.bsId = b.bsId " +
            "JOIN Route r1 ON r1.routeId = r.routeId WHERE r1.routeNo =:routeNo")
    List<BusStopInfo> findByRouteNo(@Param("routeNo") String routeNo);


}
