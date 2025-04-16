package kroryi.bus2.controller.mypage;

import kroryi.bus2.entity.mypage.FavoriteBusStop;
import kroryi.bus2.entity.mypage.FavoriteRoute;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.mypage.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/api/mypage/favorites")
@RequiredArgsConstructor
public class MypageFavoriteController {
    private final FavoriteService favoriteService;

    @GetMapping("/busStops")
    public List<FavoriteBusStop> getFavoriteBusStops(@AuthenticationPrincipal User user) {
        return favoriteService.getFavoriteBusStops(user);
    }

    @GetMapping("/routes")
    public List<FavoriteRoute> getFavoriteRoutes(@AuthenticationPrincipal User user) {
        return favoriteService.getFavoriteRoutes(user);
    }
}
