package kroryi.bus2.repository.jpa.mypage;

import kroryi.bus2.entity.mypage.FavoriteRoute;
import kroryi.bus2.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FavoriteRouteRepository extends JpaRepository<FavoriteRoute, Long> {

    // 특정 사용자의 즐겨찾는 노선 목록 조회
    @Query("SELECT f FROM FavoriteRoute f JOIN FETCH f.user JOIN FETCH f.route")
    List<FavoriteRoute> findByUser(User user);

    // 중복 방지를 위한 조회 (특정 사용자 + 노선)
    boolean existsByUserAndRouteId(User user, String routeId);  // 필요시 사용
}
