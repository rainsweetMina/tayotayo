package kroryi.bus2.repository.jpa.route;

import kroryi.bus2.entity.CustomRoute;
import kroryi.bus2.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomRouteRepository extends JpaRepository<CustomRoute, Long> {
    boolean existsByRouteId(String routeId);

    @Query("SELECT r FROM CustomRoute r WHERE r.routeNo LIKE %:routeNo% OR REPLACE(r.routeNo, ' ', '') LIKE %:routeNo% ORDER BY r.routeNo ASC")
    List<CustomRoute> searchByRouteNumberFull(@Param("routeNo") String routeNo);

    Optional<CustomRoute> findByRouteId(String routeId);

    void deleteByRouteId(String routeId);

}