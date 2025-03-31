package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route,Long> {

    @Query("SELECT r.routeNo FROM Route r WHERE r.routeNo LIKE %:routeNo% OR REPLACE(r.routeNo, ' ', '') LIKE %:routeNo%")
    List<String> searchByRouteNumber(@Param("routeNo") String routeNo);

    @Query("SELECT r FROM Route r WHERE r.routeNo LIKE %:routeNo% OR REPLACE(r.routeNo, ' ', '') LIKE %:routeNo% ORDER BY r.routeNo ASC")
    List<Route> searchByRouteNumberFull(@Param("routeNo") String routeNo);


    @Query("SELECT r.routeId FROM Route r WHERE r.routeNo = :routeNo")
    List<String> findRouteIdsByRouteNo(@Param("routeNo") String routeNo);


}
