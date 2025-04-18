package kroryi.bus2.repository.jpa.route;

import kroryi.bus2.entity.route.RouteDelete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteDeleteRepository extends JpaRepository<RouteDelete, Long> {
}
