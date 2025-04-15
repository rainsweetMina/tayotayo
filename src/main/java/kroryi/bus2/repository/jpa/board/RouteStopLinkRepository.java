package kroryi.bus2.repository.jpa.board;

import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.entity.route.RouteStopLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteStopLinkRepository extends JpaRepository<RouteStopLink,Long> {
    boolean existsByRouteIdAndSeq(String routeId, int seq);

    @Query("SELECT DISTINCT r.moveDir FROM RouteStopLink r WHERE r.routeId = :routeId")
    List<String> findDistinctMoveDirByRouteId(@Param("routeId") String routeId);

    @Query(
            value = "SELECT r.seq, b.bs_nm, r.bs_id, r.x_pos, r.y_pos " +
                    "FROM route_stop_link r " +
                    "JOIN bus_stop b ON r.bs_id = b.bs_id " +
                    "WHERE r.route_id = :routeId " +
                    "ORDER BY r.seq ASC",
            nativeQuery = true)
    List<Object[]> findRawStopDataByRouteId(@Param("routeId") String routeId);

    @Query("""
                SELECT r.seq, b.bsNm, r.bsId, r.xPos, r.yPos
                FROM RouteStopLink r
                JOIN BusStop b ON r.bsId = b.bsId
                WHERE r.routeId = :routeId AND r.moveDir = :moveDir
                ORDER BY r.seq ASC
            """)
    List<Object[]> findRawStopDataByRouteIdAndMoveDir(@Param("routeId") String routeId,
                                                      @Param("moveDir") String moveDir);


    @Query("SELECT r.routeId FROM Route r " +
            "WHERE r.routeNo = :routeNo AND r.routeId IN (" +
            "SELECT DISTINCT l.routeId FROM RouteStopLink l WHERE l.moveDir = :moveDir)")
    String findRouteIdByRouteNoAndMoveDir(@Param("routeNo") String routeNo,
                                          @Param("moveDir") String moveDir);

    // 예시 쿼리: moveDir만 추출
    @Query("SELECT DISTINCT r.moveDir FROM RouteStopLink r WHERE r.routeId IN (SELECT ro.routeId FROM Route ro WHERE ro.routeNo = :routeNo)")
    List<String> findDistinctMoveDirsByRouteNo(@Param("routeNo") String routeNo);

    @Query("SELECT DISTINCT l.routeId FROM RouteStopLink l " +
            "WHERE l.routeId IN (SELECT r.routeId FROM Route r WHERE r.routeNo = :routeNo) " +
            "AND l.moveDir = :moveDir")
    List<String> findDistinctRouteIdByRouteNoAndMoveDir(@Param("routeNo") String routeNo, @Param("moveDir") String moveDir);


    Optional<RouteStopLink> findByRouteIdAndBsIdAndMoveDir(String routeId, String bsId, String moveDir);

    List<RouteStopLink> findByRouteIdAndMoveDir(String routeId, String moveDir);

    @Query("SELECT COALESCE(MAX(r.seq), 0) FROM RouteStopLink r WHERE r.routeId = :routeId AND r.moveDir = :moveDir")
    int findMaxSeqByRouteIdAndMoveDir(@Param("routeId") String routeId, @Param("moveDir") String moveDir);

    Optional<RouteStopLink> findByRouteIdAndMoveDirAndSeq(String routeId, String moveDir, int seq);


    RouteStopLink findByRouteId(String routeId);

    @Query("SELECT DISTINCT r.routeId FROM RouteStopLink r WHERE r.bsId = :bsId")
    List<String> findRouteIdsByBusStopId(@Param("bsId") String bsId);

    int countByBsId(String bsId);



    // 직통으로 찾아주는 쿼리들
    @Query("""
SELECT new kroryi.bus2.dto.busStop.BusStopDTO(
    rsl.seq, bs.bsNm, bs.bsId, bs.xPos, bs.yPos)
FROM RouteStopLink rsl
JOIN BusStop bs ON rsl.bsId = bs.bsId
WHERE rsl.routeId = :routeId
  AND rsl.moveDir = :moveDir
  AND rsl.seq BETWEEN 
    (SELECT MIN(r1.seq) FROM RouteStopLink r1 
     WHERE r1.routeId = :routeId AND r1.bsId = :startBsId AND r1.moveDir = :moveDir)
    AND 
    (SELECT MAX(r2.seq) FROM RouteStopLink r2 
     WHERE r2.routeId = :routeId AND r2.bsId = :endBsId AND r2.moveDir = :moveDir)
ORDER BY rsl.seq
""")
    List<BusStopDTO> findStationInfoBetweenWithDirection(
            @Param("routeId") String routeId,
            @Param("startBsId") String startBsId,
            @Param("endBsId") String endBsId,
            @Param("moveDir") String moveDir);

    @Query("""
SELECT rsl.moveDir 
FROM RouteStopLink rsl
WHERE rsl.routeId = :routeId
  AND rsl.bsId = :bsId
""")
    List<String> findMoveDirByRouteIdAndBsId(@Param("routeId") String routeId,
                                       @Param("bsId") String bsId);

    @Query("""
SELECT r1.routeId
FROM RouteStopLink r1
JOIN RouteStopLink r2 ON r1.routeId = r2.routeId AND r1.moveDir = r2.moveDir
WHERE r1.bsId = :startBsId
  AND r2.bsId = :endBsId
  AND r1.seq < r2.seq
""")
    List<String> findDirectRouteIdsWithSeqAndDir(String startBsId, String endBsId);




// 환승 쿼리들
    // 출발 정류장에서 직통으로 갈 수 있는 중간 정류장들
    @Query("""
    SELECT DISTINCT rsl2.bsId
    FROM RouteStopLink rsl1
    JOIN RouteStopLink rsl2 ON rsl1.routeId = rsl2.routeId
    WHERE rsl1.bsId = :startBsId
      AND rsl1.moveDir = rsl2.moveDir
      AND rsl1.seq < rsl2.seq
""")
    List<String> findReachableStopsFrom(@Param("startBsId") String startBsId);

    // 도착 정류장으로 올 수 있는 중간 정류장들
    @Query("""
    SELECT DISTINCT rsl1.bsId FROM RouteStopLink rsl1
    JOIN RouteStopLink rsl2 ON rsl1.routeId = rsl2.routeId AND rsl1.moveDir = rsl2.moveDir
    WHERE rsl2.bsId = :endBsId AND rsl1.seq < rsl2.seq
""")
    List<String> findReachableStopsTo(@Param("endBsId") String endBsId);




    @Query("""
SELECT DISTINCT r2.bsId
FROM RouteStopLink r1
JOIN RouteStopLink r2 ON r1.routeId = r2.routeId
WHERE r1.bsId = :startBsId
  AND r1.moveDir = r2.moveDir
  AND r1.seq < r2.seq
""")
    List<String> findReachableStopsFromWithDirection(@Param("startBsId") String startBsId);

    @Query("""
SELECT DISTINCT r1.bsId
FROM RouteStopLink r1
JOIN RouteStopLink r2 ON r1.routeId = r2.routeId
WHERE r2.bsId = :endBsId
  AND r1.moveDir = r2.moveDir
  AND r1.seq < r2.seq
""")
    List<String> findReachableStopsToWithDirection(@Param("endBsId") String endBsId);


}
