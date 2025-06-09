package kroryi.bus2.controller.mypage;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kroryi.bus2.config.security.CustomUserDetails;
import kroryi.bus2.dto.mypage.ChangePasswordDTO;
import kroryi.bus2.dto.mypage.ModifyUserDTO;
import kroryi.bus2.dto.user.UserInfoDTO;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;

    // ✅ 로그인된 유저 정보 반환
    @Hidden
    @GetMapping("/user/info")
    public ResponseEntity<?> userInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패");
        }

        User user = userDetails.getUser();

        UserInfoDTO response = new UserInfoDTO(
                user.getId(),
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getSignupType(),
                user.getSignupDate(),
                user.getRole().name(),
                user.getLastLoginAt()
        );

        return ResponseEntity.ok(response);
    }

    // ✅ 회원 정보 수정
    @PostMapping("/mypage/modify")
    public ResponseEntity<?> modifyUser(@RequestBody ModifyUserDTO dto,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getUser().getUserId();
        userService.modifyUserInfo(userId, dto); // <- 이 메서드가 UserService에 있어야 함
        return ResponseEntity.ok().build();
    }

    // ✅ 비밀번호 변경
    @PostMapping("/mypage/password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordDTO dto,
                                            @AuthenticationPrincipal CustomUserDetails userDetails,
                                            HttpServletRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        String userId = userDetails.getUser().getUserId();

        try {
            userService.changePassword(
                    userId,
                    dto.getCurrentPassword(),
                    dto.getModifyPassword(),
                    dto.getModifyPasswordCheck()
            );

            // ✅ 세션 무효화 → 로그아웃
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
