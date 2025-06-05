package kroryi.bus2.controller.mypage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.config.security.CustomOAuth2User;
import kroryi.bus2.config.security.CustomUserDetails;
import kroryi.bus2.dto.mypage.FavoriteBusStopRequestDTO;
import kroryi.bus2.dto.mypage.FavoriteBusStopResponseDTO;
import kroryi.bus2.dto.mypage.FavoriteRouteRequestDTO;
import kroryi.bus2.dto.mypage.FavoriteRouteResponseDTO;
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
import kroryi.bus2.service.mypage.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
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
    private final NotificationService notificationService;

    @Operation(summary = "정류장 즐겨찾기 추가")
    @PostMapping("/favorite/bus-stop")
    @ResponseBody
    public ResponseEntity<String> addFavoriteBusStop(@RequestBody FavoriteBusStopRequestDTO dto,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("\uD83D\uDCE9 받은 요청 bsId = {}", dto.getBsId());
        String loginUserId = userDetails.getUserId();

        User user = userRepository.findByUserId(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (favoriteBusStopRepository.existsByUserUserIdAndBsId(loginUserId, dto.getBsId())) {
            return ResponseEntity.badRequest().body("이미 즐겨찾기에 추가된 정류장입니다.");
        }

        BusStop busStop = busStopRepository.findByBsId(dto.getBsId())
                .orElseThrow(() -> new IllegalArgumentException("정류장을 찾을 수 없습니다."));

        FavoriteBusStop favorite = new FavoriteBusStop(null, dto.getBsId(), loginUserId, busStop, user, LocalDateTime.now());
        favoriteBusStopRepository.save(favorite);

        // 상세 메시지로 변경
        String message = String.format("'%s' 정류장이 즐겨찾기에 추가되었습니다.", busStop.getBsNm());
        notificationService.createNotification(loginUserId, message);

        return ResponseEntity.ok("정류장이 즐겨찾기에 추가되었습니다.");
    }

    @Operation(summary = "정류장 즐겨찾기 삭제")
    @DeleteMapping("/favorite/bus-stop/{bsId}")
    public ResponseEntity<?> deleteFavoriteBusStop(@PathVariable String bsId,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String userId = userDetails.getUserId();

        if (!favoriteBusStopRepository.existsByUserUserIdAndBsId(userId, bsId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("즐겨찾기 항목이 없습니다.");
        }

        // 삭제 전에 정류장 이름 가져오기
        BusStop busStop = busStopRepository.findByBsId(bsId)
                .orElse(null);
        favoriteBusStopRepository.deleteByUserUserIdAndBsId(userId, bsId);

        String message = busStop != null ?
                String.format("'%s' 정류장이 즐겨찾기에서 삭제되었습니다.", busStop.getBsNm()) :
                "정류장 즐겨찾기가 삭제되었습니다.";

        notificationService.createNotification(userId, message);

        return ResponseEntity.ok(message);
    }

    @Operation(summary = "노선 즐겨찾기 추가")
    @PostMapping("/favorite/route")
    @ResponseBody
    public ResponseEntity<String> addFavoriteRoute(@RequestBody FavoriteRouteRequestDTO dto,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        String loginUserId = userDetails.getUserId();

        User user = userRepository.findByUserId(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (favoriteRouteRepository.existsByUserUserIdAndRouteId(loginUserId, dto.getRouteId())) {
            return ResponseEntity.badRequest().body("이미 즐겨찾기에 추가된 노선입니다.");
        }

        Route route = routeRepository.findByRouteId(dto.getRouteId())
                .orElseThrow(() -> new IllegalArgumentException("노선을 찾을 수 없습니다."));

        FavoriteRoute favorite = new FavoriteRoute(null, dto.getRouteId(), loginUserId, route, user, LocalDateTime.now());
        favoriteRouteRepository.save(favorite);

        // 상세 메시지로 변경
        String message = String.format("'%s'번 노선이 즐겨찾기에 추가되었습니다.", route.getRouteNo());
        notificationService.createNotification(loginUserId, message);

        return ResponseEntity.ok("노선이 즐겨찾기에 추가되었습니다.");
    }

    @Operation(summary = "노선 즐겨찾기 삭제")
    @DeleteMapping("/favorite/route/{routeId}")
    public ResponseEntity<?> deleteFavoriteRoute(@PathVariable String routeId,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String userId = userDetails.getUserId();

        if (!favoriteRouteRepository.existsByUserUserIdAndRouteId(userId, routeId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("즐겨찾기 항목이 없습니다.");
        }

        // 삭제 전에 노선 정보 가져오기
        Route route = routeRepository.findByRouteId(routeId)
                .orElse(null);

        favoriteRouteRepository.deleteByUserUserIdAndRouteId(userId, routeId);

        String message = route != null ?
                String.format("'%s'번 노선(%s → %s)이 즐겨찾기에서 삭제되었습니다.",
                        route.getRouteNo(), route.getStNm(), route.getEdNm()) :
                "노선 즐겨찾기가 삭제되었습니다.";

        notificationService.createNotification(userId, message);

        return ResponseEntity.ok(message);
    }

    @Operation(summary = "정류장 즐겨찾기 목록 조회")
    @GetMapping("/favorite/bus-stop")
    public ResponseEntity<List<FavoriteBusStopResponseDTO>> getFavoriteBusStops(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String userId = userDetails.getUserId();

        List<FavoriteBusStop> list = favoriteBusStopRepository.findByUserUserId(userId);
        List<FavoriteBusStopResponseDTO> dtoList = list.stream()
                .map(f -> new FavoriteBusStopResponseDTO(f.getBsId(), f.getBusStop().getBsNm()))
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "노선 즐겨찾기 목록 조회")
    @GetMapping("/favorite/route")
    public ResponseEntity<List<FavoriteRouteResponseDTO>> getFavoriteRoutes(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String userId = userDetails.getUserId();

        List<FavoriteRoute> favorites = favoriteRouteRepository.findByUserUserId(userId);
        List<FavoriteRouteResponseDTO> dtoList = favorites.stream()
                .map(f -> new FavoriteRouteResponseDTO(f.getRouteId(), f.getRoute().getRouteNo(), f.getRoute().getStNm(), f.getRoute().getEdNm()))
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "노선,정류장 통합 조회")
    @GetMapping("/favorite/all")
    @ResponseBody
    public ResponseEntity<Map<String, List<?>>> getAllFavorites(@RequestParam String userId) {
        Map<String, List<?>> result = favoriteService.getFavoriteWithInfoByUserId(userId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "즐겨찾기 페이지")
    @GetMapping("/favorites")
    public String showFavoritesPage(Model model, @AuthenticationPrincipal Object principal) {
        String userId = null;
        if (principal instanceof CustomOAuth2User customUser) {
            userId = customUser.getUserId();
        } else if (principal instanceof CustomUserDetails userDetails) {
            userId = userDetails.getUsername();
        }

        if (userId == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("userId", userId);
        return "mypage/mypage-favorites";
    }

    @GetMapping("/favorites/summary")
    public ResponseEntity<?> getFavoriteSummary(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        String userId = userDetails.getUser().getUserId();

        try {
            int busCount = favoriteRouteRepository.countByUser_UserId(userId);
            int stopCount = favoriteBusStopRepository.countByUser_UserId(userId);

            Map<String, Integer> result = new HashMap<>();
            result.put("busCount", busCount);
            result.put("stopCount", stopCount);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ 즐겨찾기 요약 로딩 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버 오류가 발생했습니다."));
        }
    }
}
