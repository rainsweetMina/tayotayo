package kroryi.bus2.repository.jpa;

import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.entity.BusStop;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusStopRepository extends JpaRepository<BusStop,Long> {

    @Query("SELECT b FROM BusStop b")
    List<BusStop> findBusStops();



    List<BusStop> findByBsNmContaining(String bsNm);

    @Query("SELECT b FROM BusStop b WHERE b.bsNm LIKE %:bsNm% OR REPLACE(b.bsNm, ' ', '') LIKE %:bsNm%")
    List<BusStop> searchByBsNmIgnoreSpace(@Param("bsNm") String bsNm);

    Optional<BusStop> findByBsId(String bsId);

    boolean existsByBsId(String bsId);

    @Query("SELECT new kroryi.bus2.dto.busStop.BusStopDTO(b.bsId, b.bsNm, b.xPos, b.yPos) " +
            "FROM BusStop b " +
            "WHERE b.xPos BETWEEN :minX AND :maxX " +
            "AND b.yPos BETWEEN :minY AND :maxY")
    List<BusStopDTO> findInBounds(@Param("minX") double minX,
                                  @Param("maxX") double maxX,
                                  @Param("minY") double minY,
                                  @Param("maxY") double maxY);

    @Query("SELECT COUNT(r) FROM RouteStopLink r WHERE r.bsId = :bsId")
    int countByBsId(@Param("bsId") String bsId);

    void deleteByBsId(String bsId);
}
