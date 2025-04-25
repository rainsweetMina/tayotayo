package kroryi.bus2.repository.jpa.mypage;

import jakarta.transaction.Transactional;
import kroryi.bus2.entity.mypage.FavoriteRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRouteRepository extends JpaRepository<FavoriteRoute, Long> {

    // 특정 사용자 ID로 즐겨찾는 노선 + Route 정보까지 함께 조회 (JOIN FETCH)
    @Query("SELECT f FROM FavoriteRoute f JOIN FETCH f.user u JOIN FETCH f.route r WHERE u.userId = :userId")
    List<FavoriteRoute> findWithRouteByUserId(@Param("userId") String userId);

    // 기본 findBy (userId만 기준)
    List<FavoriteRoute> findByUser_UserId(String userId);

    // 중복 방지를 위한 존재 여부 체크
    boolean existsByUserUserIdAndRouteId(String userId, String routeId);

    // 삭제
    @Transactional
    @Modifying
    int deleteByUserUserIdAndRouteId(String userId, String routeId);
}
