package kroryi.bus2.repository.jpa.route;

import kroryi.bus2.dto.Route.RouteDTO;
import kroryi.bus2.dto.Route.RouteIdAndNoDTO;
import kroryi.bus2.entity.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Optional<Route> findByRouteId(String routeId);

    @Query("SELECT r.routeId FROM Route r WHERE r.routeNo = :routeNo")
    List<String> findRouteIdsByRouteNo(@Param("routeNo") String routeNo);
    void deleteByRouteId(String routeId);

    // 버스 스케줄 노선 조회 쿼리
    @Query("SELECT DISTINCT TRIM(r.routeNo) FROM Route r ORDER BY TRIM(r.routeNo)")
    List<String> findDistinctRouteNos();

    // 버스 스케줄 노선 방면 조회 쿼리
    @Query("SELECT DISTINCT r.routeNote FROM Route r WHERE r.routeNo = :routeNo AND r.routeNote IS NOT NULL")
    List<String> findDistinctRouteNoteByRouteNo(@Param("routeNo") String routeNo);

    @Query("SELECT r.routeId FROM Route r WHERE r.routeNo = :routeNo AND r.routeNote = :routeNote")
    String findRouteIdByRouteNoAndNote(@Param("routeNo") String routeNo, @Param("routeNote") String routeNote);

    @Query("SELECT r.routeId FROM Route r")
    List<String> findAllRouteIds();

    @Query("SELECT r.routeId FROM Route r WHERE r.routeNo = :routeNo")
    String findRouteIdByRouteNoOnly(@Param("routeNo") String routeNo);

    boolean existsByRouteId(String routeId);

    @Query("SELECT new kroryi.bus2.dto.Route.RouteIdAndNoDTO(r.routeId, r.routeNo) FROM Route r WHERE r.routeId IN :routeIds")
    List<RouteIdAndNoDTO> findRoutesByIds(@Param("routeIds") List<String> routeIds);

    // 페이징 + 검색이 추가된 전체 노선 게시판 레파지토리들
    Page<Route> findAll(Pageable pageable);

    @Query("SELECT r FROM Route r WHERE " +
            "LOWER(r.routeId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.routeNo) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Route> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

}
