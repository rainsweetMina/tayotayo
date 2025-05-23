package kroryi.bus2.controller.mypage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.config.security.CustomOAuth2User;
import kroryi.bus2.config.security.CustomUserDetails;
import kroryi.bus2.entity.busStop.BusStop;
import kroryi.bus2.entity.mypage.FavoriteBusStop;
import kroryi.bus2.entity.mypage.FavoriteRoute;
import kroryi.bus2.entity.route.Route;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.UserRepository;
import kroryi.bus2.repository.jpa.bus_stop.BusStopRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import kroryi.bus2.repository.jpa.mypage.FavoriteBusStopRepository;
import kroryi.bus2.repository.jpa.mypage.FavoriteRouteRepository;
import kroryi.bus2.service.mypage.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "마이페이지-즐겨찾기", description = "정류장 및 노선 즐겨찾기 추가/삭제/조회 기능을 제공합니다.")
@Controller
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MypageFavoriteController {

    private final FavoriteBusStopRepository favoriteBusStopRepository;
    private final FavoriteRouteRepository favoriteRouteRepository;
    private final BusStopRepository busStopRepository;
    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final FavoriteService favoriteService;

    @Operation(summary = "정류장 즐겨찾기 추가", description = "사용자가 특정 정류장을 즐겨찾기에 추가합니다.")
    @PostMapping("/favorite/bus-stop")
    @ResponseBody
    public ResponseEntity<String> addFavoriteBusStop(@RequestParam String userId, @RequestParam String bsId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (favoriteBusStopRepository.existsByUserUserIdAndBsId(userId, bsId)) {
            return ResponseEntity.badRequest().body("이미 즐겨찾기에 추가된 정류장입니다.");
        }

        BusStop busStop = busStopRepository.findByBsId(bsId)
                .orElseThrow(() -> new IllegalArgumentException("정류장을 찾을 수 없습니다."));
        FavoriteBusStop favorite = new FavoriteBusStop(null, bsId, user.getUserId(), busStop, user, LocalDateTime.now());
        favoriteBusStopRepository.save(favorite);
        return ResponseEntity.ok("정류장이 즐겨찾기에 추가되었습니다.");
    }

    @Operation(summary = "정류장 즐겨찾기 삭제", description = "사용자가 특정 정류장을 즐겨찾기에서 삭제합니다.")
    @DeleteMapping("/favorite/bus-stop")
    @ResponseBody
    public ResponseEntity<String> removeFavoriteBusStop(@RequestParam String userId, @RequestParam String bsId) {
        int deletedCount = favoriteBusStopRepository.deleteByUserUserIdAndBsId(userId, bsId);
        if (deletedCount == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 정류장은 즐겨찾기에서 삭제된 상태이거나 존재하지 않습니다.");
        }
        return ResponseEntity.ok("정류장이 즐겨찾기에서 삭제되었습니다.");
    }

    @Operation(summary = "정류장 즐겨찾기 목록 조회", description = "사용자가 즐겨찾는 정류장 목록을 조회합니다.")
    @GetMapping("/favorite/bus-stop")
    @ResponseBody
    public ResponseEntity<List<FavoriteBusStop>> getFavoriteBusStops(@RequestParam String userId) {
        List<FavoriteBusStop> busStops = favoriteBusStopRepository.findByUser_UserId(userId);
        return ResponseEntity.ok(busStops);
    }

    @Operation(summary = "노선 즐겨찾기 추가", description = "사용자가 특정 노선을 즐겨찾기에 추가합니다.")
    @PostMapping("/favorite/route")
    @ResponseBody
    public ResponseEntity<String> addFavoriteRoute(@RequestParam String userId, @RequestParam String routeId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (favoriteRouteRepository.existsByUserUserIdAndRouteId(userId, routeId)) {
            return ResponseEntity.badRequest().body("이미 즐겨찾기에 추가된 노선입니다.");
        }

        Route route = routeRepository.findByRouteId(routeId)
                .orElseThrow(() -> new IllegalArgumentException("노선을 찾을 수 없습니다."));
        FavoriteRoute favorite = new FavoriteRoute(null, routeId, user.getUserId(), route, user, LocalDateTime.now());
        favoriteRouteRepository.save(favorite);
        return ResponseEntity.ok("노선이 즐겨찾기에 추가되었습니다.");
    }

    @Operation(summary = "노선 즐겨찾기 삭제", description = "사용자가 특정 노선을 즐겨찾기에서 삭제합니다.")
    @DeleteMapping("/favorite/route")
    @ResponseBody
    public ResponseEntity<String> removeFavoriteRoute(@RequestParam String userId, @RequestParam String routeId) {
        int deletedCount = favoriteRouteRepository.deleteByUserUserIdAndRouteId(userId, routeId);
        if (deletedCount == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 노선은 즐겨찾기에서 삭제된 상태이거나 존재하지 않습니다.");
        }
        return ResponseEntity.ok("노선이 즐겨찾기에서 삭제되었습니다.");
    }

    @Operation(summary = "노선 즐겨찾기 목록 조회", description = "사용자가 즐겨찾는 노선 목록을 조회합니다.")
    @GetMapping("/favorite/route")
    @ResponseBody
    public ResponseEntity<List<FavoriteRoute>> getFavoriteRoutes(@RequestParam String userId) {
        List<FavoriteRoute> routes = favoriteRouteRepository.findByUser_UserId(userId);
        return ResponseEntity.ok(routes);
    }

    @Operation(summary = "노선,정류장 통합 조회", description = "사용자가 즐겨찾는 정류장과 노선 목록을 통합 조회합니다.")
    @GetMapping("/favorite/all")
    @ResponseBody
    public ResponseEntity<Map<String, List<?>>> getAllFavorites(@RequestParam String userId) {
        Map<String, List<?>> result = favoriteService.getFavoriteWithInfoByUserId(userId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "즐겨찾기 페이지", description = "마이페이지 즐겨찾기 뷰 페이지를 반환합니다.")
    @GetMapping("/favorites")
    public String showFavoritesPage(Model model, @AuthenticationPrincipal Object principal) {
        String userId = null;
        if (principal instanceof CustomOAuth2User customUser) {
            userId = customUser.getUserId();
        } else if (principal instanceof CustomUserDetails userDetails) {
            userId = userDetails.getUsername();
        }

        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("userId", userId);
        return "mypage/mypage-favorites";
    }
}
