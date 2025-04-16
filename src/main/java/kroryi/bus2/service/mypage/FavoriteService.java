package kroryi.bus2.service.mypage;

import kroryi.bus2.entity.mypage.FavoriteBusStop;
import kroryi.bus2.entity.mypage.FavoriteRoute;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.mypage.FavoriteBusStopRepository;
import kroryi.bus2.repository.jpa.mypage.FavoriteRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteBusStopRepository favoriteBusStopRepository;
    private final FavoriteRouteRepository favoriteRouteRepository;

    // 사용자 즐겨찾기 정류장 목록 조회
    public List<FavoriteBusStop> getFavoriteBusStops(User user) {
        return favoriteBusStopRepository.findByUser(user);
    }

    // 사용자 즐겨찾기 노선 목록 조회
    public List<FavoriteRoute> getFavoriteRoutes(User user) {
        return favoriteRouteRepository.findByUser(user);
    }
}
