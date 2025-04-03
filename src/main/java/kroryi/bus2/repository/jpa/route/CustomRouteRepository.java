package kroryi.bus2.repository.jpa.route;

import kroryi.bus2.entity.CustomRoute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomRouteRepository extends JpaRepository<CustomRoute, Long> {
    boolean existsByRouteId(String routeId);

}