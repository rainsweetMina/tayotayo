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

    @Query(
            value = "SELECT r.seq, b.bs_nm, r.bs_id, r.x_pos, r.y_pos " +
                    "FROM route_stop_link r " +
                    "JOIN bus_stop b ON r.bs_id = b.bs_id " +
                    "WHERE r.route_id = :routeId " +
                    "ORDER BY r.seq ASC",
            nativeQuery = true)
    List<Object[]> findRawStopDataByRouteId(@Param("routeId") String routeId);

    Optional<RouteStopLink> findByRouteIdAndBsIdAndMoveDir(String routeId, String bsId, String moveDir);

    List<RouteStopLink> findByRouteIdAndMoveDir(String routeId, String moveDir);

    @Query("SELECT COALESCE(MAX(r.seq), 0) FROM RouteStopLink r WHERE r.routeId = :routeId AND r.moveDir = :moveDir")
    int findMaxSeqByRouteIdAndMoveDir(@Param("routeId") String routeId, @Param("moveDir") String moveDir);

    Optional<RouteStopLink> findByRouteIdAndMoveDirAndSeq(String routeId, String moveDir, int seq);


}
