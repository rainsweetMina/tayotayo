package kroryi.bus2.controller.mypage;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpSession;
import kroryi.bus2.config.security.CustomUserDetails;
import kroryi.bus2.dto.user.UserInfoDTO;
import kroryi.bus2.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {

    // 로그인된 유저 정보 반환
    @Hidden
    @GetMapping("/info")
    public ResponseEntity<?> userInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userDetails.getUser().getUserId());
        result.put("username", userDetails.getUser().getUsername());
        result.put("email", userDetails.getUser().getEmail());
        result.put("role", userDetails.getUser().getRole().name());

        return ResponseEntity.ok(result);
    }




}
