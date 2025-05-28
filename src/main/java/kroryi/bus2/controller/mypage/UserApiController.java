package kroryi.bus2.controller.mypage;

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
    @GetMapping("/info")
    public ResponseEntity<?> userInfo(HttpSession session) {
        User user = (User) session.getAttribute("user"); // ✅ 로그인 시 저장한 값
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        return ResponseEntity.ok(result);
    }


}
