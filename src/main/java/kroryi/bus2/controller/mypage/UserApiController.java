package kroryi.bus2.controller.mypage;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kroryi.bus2.config.security.CustomOAuth2User;
import kroryi.bus2.config.security.CustomUserDetails;
import kroryi.bus2.dto.mypage.ChangePasswordDTO;
import kroryi.bus2.dto.mypage.ModifyUserDTO;
import kroryi.bus2.dto.user.JoinRequestDTO;
import kroryi.bus2.dto.user.PasswordRequestDTO;
import kroryi.bus2.dto.user.UserInfoDTO;
import kroryi.bus2.entity.user.SignupType;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.lost.LostItemService;
import kroryi.bus2.service.user.EmailService;
import kroryi.bus2.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;
    private final EmailService emailService;
    private final LostItemService lostItemService;

    // ✅ 로그인된 유저 정보 반환
    @Hidden
    @GetMapping("/user/info")
    public ResponseEntity<?> userInfo(@AuthenticationPrincipal Object principal, HttpServletRequest request) {
        log.info("✅ /api/user/info 호출됨");

        if (principal == null) {
            log.warn("❌ 인증 정보 없음 (principal=null)");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패");
        }

        log.info("🔎 인증 주체 클래스: {}", principal.getClass().getName());

        if (principal instanceof CustomUserDetails userDetails) {
            User user = userDetails.getUser();
            return ResponseEntity.ok(new UserInfoDTO(
                    user.getId(),
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getSignupType(),
                    user.getSignupDate(),
                    user.getRole().name(),
                    user.getLastLoginAt()
            ));
        }

        if (principal instanceof CustomOAuth2User oauth2User) {
            return ResponseEntity.ok(Map.of(
                    "id", oauth2User.getUserId(),
                    "userId", oauth2User.getUserId(),
                    "username", oauth2User.getNickname(),
                    "email", oauth2User.getEmail(),
                    "role", oauth2User.getRole()
            ));
        }

        // 예상하지 못한 principal
        log.error("⚠️ 예상치 못한 principal 타입: {}", principal.getClass().getName());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("지원하지 않는 사용자 타입");
    }

    // ✅ 회원 정보 수정
    @PostMapping("/mypage/modify")
    public ResponseEntity<?> modifyUser(
            @AuthenticationPrincipal Object principal,
            @RequestBody ModifyUserDTO dto
    ) {
        log.info("🔐 [modifyUser] 인증 사용자: {}", principal);

        if (principal == null) {
            log.warn("❌ 인증 실패: 사용자 정보 없음");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            String userId;

            if (principal instanceof CustomUserDetails userDetails) {
                userId = userDetails.getUser().getUserId();
            } else if (principal instanceof CustomOAuth2User oauth2User) {
                userId = oauth2User.getUserId();
            } else {
                log.warn("❌ 알 수 없는 인증 사용자 타입: {}", principal.getClass().getName());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "알 수 없는 사용자 타입입니다."));
            }

            log.info("📦 [modifyUser] 요청 DTO: {}", dto);
            userService.modifyUserInfo(userId, dto);
            log.info("✅ [modifyUser] 사용자 정보 수정 완료: {}", userId);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("❌ [modifyUser] 사용자 정보 수정 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버 오류가 발생했습니다."));
        }
    }


    // ✅ 비밀번호 변경
    @PostMapping("/mypage/password")
    public ResponseEntity<?> changePassword(
            @RequestBody @Valid ChangePasswordDTO dto,
            @AuthenticationPrincipal Object principal,
            HttpServletRequest request
    ) {
        String userId = null;

        if (principal instanceof CustomUserDetails userDetails) {
            User user = userDetails.getUser();

            // ✅ 비밀번호 방식 회원이 아닐 경우
            if (user.getSignupType() != SignupType.GENERAL) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다."));
            }

            userId = user.getUserId();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            userService.changePassword(
                    userId,
                    dto.getCurrentPassword(),
                    dto.getModifyPassword(),
                    dto.getModifyPasswordCheck()
            );

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate(); // ✅ 변경 후 세션 무효화
            }

            return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다. 다시 로그인해주세요."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버 오류가 발생했습니다."));
        }
    }

//    // 디버깅용
//    @GetMapping("/mypage/debug-session")
//    public ResponseEntity<?> debugSession(HttpServletRequest request) {
//        log.info("⚙️ /debug-session 진입");
//
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            for (Cookie c : cookies) {
//                log.info("🍪 쿠키: {} = {}", c.getName(), c.getValue());
//            }
//        } else {
//            log.warn("❌ 쿠키 없음");
//        }
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        log.info("🔐 SecurityContext 인증: {}", auth);
//
//        return ResponseEntity.ok("ok");
//    }

    @PostMapping("/find-password")
    public ResponseEntity<String> sendTemporaryPassword(@RequestBody PasswordRequestDTO request) {
        userService.sendTemporaryPassword(request.getUserId(), request.getEmail());
        return ResponseEntity.ok("임시 비밀번호가 이메일로 전송되었습니다.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordRequestDTO dto) {
        userService.sendTemporaryPassword(dto.getUserId(), dto.getEmail());
        return ResponseEntity.ok("임시 비밀번호가 전송되었습니다.");
    }

    @Operation(summary = "회원 탈퇴 처리", description = "현재 로그인된 사용자를 탈퇴 처리합니다.")
    @PostMapping("/mypage/withdraw")
    public ResponseEntity<?> handleWithdraw(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody Map<String, String> body
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        String password = body.get("password");
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "비밀번호가 누락되었습니다."));
        }

        try {
            userService.withdrawUser(user.getUserId(), password);
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "서버 오류"));
        }
    }

    @PostMapping("/user/join")
    @ResponseBody
    public ResponseEntity<?> registerApi(@RequestBody JoinRequestDTO dto) {
        try {
            String normalizedEmail = dto.getEmail().trim().toLowerCase();
            dto.setEmail(normalizedEmail);

            // ✅ 인증 상태만 확인
            if (!emailService.isEmailVerified(normalizedEmail)) {
                return ResponseEntity.badRequest().body(Map.of("message", "이메일 인증을 먼저 완료해주세요."));
            }

            // ✅ 회원가입
            userService.join(dto);

            return ResponseEntity.ok(Map.of("message", "회원가입 완료"));

        } catch (Exception e) {
            log.error("❌ 회원가입 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("message", "서버 오류: " + e.getMessage()));
        }
    }

    @GetMapping("/mypage/lost/count")
    public ResponseEntity<?> countMyLostItems(@AuthenticationPrincipal Object principal) {

        /* ① 로그인 체크 & userId 추출 */
        String userId = null;
        if (principal instanceof CustomUserDetails cd) {
            userId = cd.getUser().getUserId();
        } else if (principal instanceof CustomOAuth2User ou) {
            userId = ou.getUserId();
        }
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        /* ② User 조회 */
        User user = userService.findByUserId(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "사용자를 찾을 수 없습니다."));
        }

        /* ③ 분실물 개수 조회 */
        long count = lostItemService.countMyLostItems(user.getId());

        /* ④ JSON 응답 */
        return ResponseEntity.ok(Map.of("count", count));
    }

}