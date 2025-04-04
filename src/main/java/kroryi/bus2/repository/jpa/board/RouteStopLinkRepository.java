package kroryi.bus2.repository.jpa.board;

import kroryi.bus2.entity.RouteStopLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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


}
