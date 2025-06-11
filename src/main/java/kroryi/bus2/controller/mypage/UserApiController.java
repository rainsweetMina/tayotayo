package kroryi.bus2.controller.mypage;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kroryi.bus2.config.security.CustomOAuth2User;
import kroryi.bus2.config.security.CustomUserDetails;
import kroryi.bus2.dto.mypage.ChangePasswordDTO;
import kroryi.bus2.dto.mypage.ModifyUserDTO;
import kroryi.bus2.dto.user.UserInfoDTO;
import kroryi.bus2.entity.user.SignupType;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;

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
    public ResponseEntity<?> modifyUser(@RequestBody ModifyUserDTO dto,
                                        @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null || principal.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = principal.getAttribute("userId");
        userService.modifyUserInfo(userId, dto);
        return ResponseEntity.ok().build();
    }

    // ✅ 비밀번호 변경
    @PostMapping("/mypage/password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordDTO dto,
                                            @AuthenticationPrincipal OAuth2User principal,
                                            HttpServletRequest request) {
        if (principal == null || principal.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        String userId = principal.getAttribute("userId");

        try {
            userService.changePassword(
                    userId,
                    dto.getCurrentPassword(),
                    dto.getModifyPassword(),
                    dto.getModifyPasswordCheck()
            );

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다. 다시 로그인해주세요."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

}
