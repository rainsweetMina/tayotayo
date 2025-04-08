package kroryi.bus2.repository.jpa.route;

import kroryi.bus2.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route,Long> {

    @Query("SELECT r.routeNo FROM Route r WHERE r.routeNo LIKE %:routeNo% OR REPLACE(r.routeNo, ' ', '') LIKE %:routeNo%")
    List<String> searchByRouteNumber(@Param("routeNo") String routeNo);

    @Query("SELECT r FROM Route r WHERE r.routeNo LIKE %:routeNo% OR REPLACE(r.routeNo, ' ', '') LIKE %:routeNo% ORDER BY r.routeNo ASC")
    List<Route> searchByRouteNumberFull(@Param("routeNo") String routeNo);


    @Query("SELECT r.routeId FROM Route r WHERE r.routeNo = :routeNo")
    List<String> findRouteIdsByRouteNo(@Param("routeNo") String routeNo);


    @Query("SELECT DISTINCT TRIM(r.routeNo) FROM Route r ORDER BY TRIM(r.routeNo)")
    List<String> findDistinctRouteNos();

    @Query("SELECT DISTINCT r.routeNote FROM Route r WHERE r.routeNo = :routeNo AND r.routeNote IS NOT NULL")
    List<String> findDistinctRouteNoteByRouteNo(@Param("routeNo") String routeNo);

    @Query("SELECT r.routeId FROM Route r WHERE r.routeNo = :routeNo AND r.routeNote = :routeNote")
    String findRouteIdByRouteNoAndNote(@Param("routeNo") String routeNo, @Param("routeNote") String routeNote);

    @Query("SELECT r.routeId FROM Route r")
    List<String> findAllRouteIds();

    @Query("SELECT r.routeId FROM Route r WHERE r.routeNo = :routeNo")
    String findRouteIdByRouteNoOnly(@Param("routeNo") String routeNo);

    boolean existsByRouteId(String routeId);

}
