package kroryi.bus2.service.mypage;

import kroryi.bus2.entity.mypage.FavoriteBusStop;
import kroryi.bus2.entity.mypage.FavoriteRoute;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.mypage.FavoriteBusStopRepository;
import kroryi.bus2.repository.jpa.mypage.FavoriteRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteBusStopRepository favoriteBusStopRepository;
    private final FavoriteRouteRepository favoriteRouteRepository;

    // ===================== 정류장 즐겨찾기 =====================

    /**
     * 정류장 즐겨찾기 추가
     */
    @Transactional
    public void addFavoriteBusStop(User user, String userId, String bsId) {
        if (!favoriteBusStopRepository.existsByUserUserIdAndBsId(userId, bsId)) {
            FavoriteBusStop favorite = new FavoriteBusStop();
            favorite.setUserId(userId);
            favorite.setBsId(bsId);
            favorite.setCreatedAt(LocalDateTime.now());
            favorite.setUser(user);
            favoriteBusStopRepository.save(favorite);
        }
    }

    /**
     * 정류장 즐겨찾기 제거
     */
    @Transactional
    public void removeFavoriteBusStop(User user, String bsId) {
        favoriteBusStopRepository.deleteByUserUserIdAndBsId(user.getUserId(), bsId);
    }

    /**
     * 정류장 즐겨찾기 목록 조회 (기본)
     */
    public List<FavoriteBusStop> getFavoriteBusStops(String userId) {
        return favoriteBusStopRepository.findByUser_UserId(userId);
    }

    /**
     * 정류장 즐겨찾기 목록 조회 (정류장 정보 포함)
     */
    public List<FavoriteBusStop> getFavoriteBusStopsWithInfo(String userId) {
        return favoriteBusStopRepository.findWithBusStopByUserId(userId);
    }

    // ===================== 노선 즐겨찾기 =====================

    /**
     * 노선 즐겨찾기 추가
     */
    @Transactional
    public void addFavoriteRoute(User user, String userId, String routeId) {
        if (!favoriteRouteRepository.existsByUserUserIdAndRouteId(userId, routeId)) {
            FavoriteRoute favorite = new FavoriteRoute();
            favorite.setUserId(userId);
            favorite.setRouteId(routeId);
            favorite.setCreatedAt(LocalDateTime.now());
            favorite.setUser(user);
            favoriteRouteRepository.save(favorite);
        }
    }

    /**
     * 노선 즐겨찾기 제거
     */
    @Transactional
    public void removeFavoriteRoute(User user, String routeId) {
        favoriteRouteRepository.deleteByUserUserIdAndRouteId(user.getUserId(), routeId);
    }

    /**
     * 노선 즐겨찾기 목록 조회 (기본)
     */
    public List<FavoriteRoute> getFavoriteRoutes(String userId) {
        return favoriteRouteRepository.findByUser_UserId(userId);
    }

    /**
     * 노선 즐겨찾기 목록 조회 (노선 정보 포함)
     */
    public List<FavoriteRoute> getFavoriteRoutesWithInfo(String userId) {
        return favoriteRouteRepository.findWithRouteByUserId(userId);
    }

    // ===================== 정류장 + 노선 통합 즐겨찾기 조회 =====================

    /**
     * 유저 ID 기준 즐겨찾기 정류장 + 노선 통합 조회
     */
    public Map<String, List<?>> getFavoriteByUserId(String userId) {
        List<FavoriteRoute> routes = favoriteRouteRepository.findByUser_UserId(userId);
        List<FavoriteBusStop> stops = favoriteBusStopRepository.findByUser_UserId(userId);

        Map<String, List<?>> result = new HashMap<>();
        result.put("routes", routes);
        result.put("busStops", stops);
        return result;
    }

    /**
     * 유저 ID 기준 즐겨찾기 정류장 + 노선 통합 조회 (상세정보 포함)
     */
    public Map<String, List<?>> getFavoriteWithInfoByUserId(String userId) {
        List<FavoriteRoute> routes = favoriteRouteRepository.findWithRouteByUserId(userId);
        List<FavoriteBusStop> stops = favoriteBusStopRepository.findWithBusStopByUserId(userId);

        Map<String, List<?>> result = new HashMap<>();
        result.put("routes", routes);
        result.put("busStops", stops);
        return result;
    }
}
