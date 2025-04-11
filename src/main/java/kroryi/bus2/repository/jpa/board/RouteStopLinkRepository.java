package kroryi.bus2.repository.jpa.board;

import kroryi.bus2.entity.Route;
import kroryi.bus2.entity.RouteStopLink;
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
}
