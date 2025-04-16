package kroryi.bus2.repository.jpa.mypage;

import kroryi.bus2.entity.mypage.FavoriteBusStop;
import kroryi.bus2.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FavoriteBusStopRepository extends JpaRepository<FavoriteBusStop, Long> {

    // 특정 사용자의 즐겨찾는 정류장 목록 조회
    @Query("SELECT f FROM FavoriteBusStop f JOIN FETCH f.user JOIN FETCH f.busStop")
    List<FavoriteBusStop> findByUser(User user);

    // 중복 방지를 위한 조회 (특정 사용자 + 정류장)
    boolean existsByUserAndBsId(User user, String bsId);

}
