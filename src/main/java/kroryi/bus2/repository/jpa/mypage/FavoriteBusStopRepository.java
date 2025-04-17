package kroryi.bus2.repository.jpa.mypage;

import kroryi.bus2.entity.mypage.FavoriteBusStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteBusStopRepository extends JpaRepository<FavoriteBusStop, Long> {

    // 특정 사용자 ID로 즐겨찾는 정류장 + BusStop 정보까지 함께 조회 (JOIN FETCH)
    @Query("SELECT f FROM FavoriteBusStop f JOIN FETCH f.user u JOIN FETCH f.busStop b WHERE u.userId = :userId")
    List<FavoriteBusStop> findWithBusStopByUserId(@Param("userId") String userId);

    // 기본 findBy (userId만 기준)
    List<FavoriteBusStop> findByUser_UserId(String userId);

    // 중복 방지를 위한 존재 여부 체크
    boolean existsByUserUserIdAndBsId(String userId, String bsId);

    // 삭제
    int deleteByUserUserIdAndBsId(String userId, String bsId);
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
//public interface FavoriteBusStopRepository extends JpaRepository<FavoriteBusStop, Long> {
////
////    // 특정 사용자의 즐겨찾는 정류장 목록 조회
////    @Query("SELECT f FROM FavoriteBusStop f JOIN FETCH f.user JOIN FETCH f.busStop WHERE f.user = :user")
////    List<FavoriteBusStop> findByUser(String userId);
////
////    // 사용자 노선 + 정류장 통합 조회
////    List<FavoriteBusStop> findByUser_UserId(@Param("userId")String userId);
////
//
//    @Query("SELECT f FROM FavoriteBusStop f JOIN FETCH f.user WHERE f.user.userId = :userId")
//    List<FavoriteBusStop> findByUser_UserId(@Param("userId") String userId);
//
//    // 중복 방지를 위한 조회 (특정 사용자 + 정류장)
//    boolean existsByUserUserIdAndBsId(String userId, String bsId);  // `User`를 통해 `userId`로 조회하는 쿼리
//
//    void deleteByUserUserIdAndBsId(String userId, String bsId);
//
//
//
//}
