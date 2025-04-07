package kroryi.bus2.repository.redis;

import kroryi.bus2.entity.Route;
import kroryi.bus2.entity.RouteRedis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RedisRouteRepository extends JpaRepository<RouteRedis,Long> {

    @Query("SELECT r.routeNo FROM RouteRedis r WHERE r.routeNo LIKE %:routeNo% OR REPLACE(r.routeNo, ' ', '') LIKE %:routeNo%")
    List<String> searchByRouteNumber(@Param("routeNo") String routeNo);

    @Query("SELECT r FROM RouteRedis r WHERE r.routeNo LIKE %:routeNo% OR REPLACE(r.routeNo, ' ', '') LIKE %:routeNo% ORDER BY r.routeNo ASC")
    List<Route> searchByRouteNumberFull(@Param("routeNo") String routeNo);


    @Query("SELECT r.routeId FROM RouteRedis r WHERE r.routeNo = :routeNo")
    List<String> findRouteIdsByRouteNo(@Param("routeNo") String routeNo);

    boolean existsByRouteId(String routeId);

}
