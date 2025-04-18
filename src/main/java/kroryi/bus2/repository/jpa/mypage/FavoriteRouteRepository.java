package kroryi.bus2.repository.jpa.mypage;

import kroryi.bus2.entity.mypage.FavoriteRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteRouteRepository extends JpaRepository<FavoriteRoute, Long> {

    // 특정 사용자 ID로 즐겨찾는 노선 + Route 정보까지 함께 조회 (JOIN FETCH)
    @Query("SELECT f FROM FavoriteRoute f JOIN FETCH f.user u JOIN FETCH f.route r WHERE u.userId = :userId")
    List<FavoriteRoute> findWithRouteByUserId(@Param("userId") String userId);

    // 기본 findBy (userId만 기준)
    List<FavoriteRoute> findByUser_UserId(String userId);

    // 중복 방지를 위한 존재 여부 체크
    boolean existsByUserUserIdAndRouteId(String userId, String routeId);

    // 삭제
    int deleteByUserUserIdAndRouteId(String userId, String routeId);
}


//package kroryi.bus2.repository.jpa.mypage;
//
//import kroryi.bus2.entity.mypage.FavoriteBusStop;
//import kroryi.bus2.entity.mypage.FavoriteRoute;
//import kroryi.bus2.entity.user.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//
//public interface FavoriteRouteRepository extends JpaRepository<FavoriteRoute, Long> {
//
////    // 특정 사용자의 즐겨찾는 노선 목록 조회
////    @Query("SELECT f FROM FavoriteRoute f JOIN FETCH f.user JOIN FETCH f.route WHERE f.userId = :userId")
////    List<FavoriteRoute> findByUser(String userId);
////
////    // 사용자 노선 + 정류장 통합 조회
////    List<FavoriteRoute> findByUser_UserId(@Param("userId")String userId);
//
//    @Query("SELECT f FROM FavoriteRoute f JOIN FETCH f.user WHERE f.user.userId = :userId")
//    List<FavoriteRoute> findByUser_UserId(@Param("userId") String userId);
//
//    // 중복 방지를 위한 조회 (특정 사용자 + 노선)
//    boolean existsByUserUserIdAndRouteId(String userId, String routeId); // `User`를 통해 `userId`로 조회하는 쿼리
//
//    void deleteByUserUserIdAndRouteId(String userId, String routeId); // `User`를 통해 `userId`로 삭제하는 쿼리
//
//
//}
