package kroryi.bus2.controller.mypage;

import kroryi.bus2.config.security.CustomUserDetails;
import kroryi.bus2.dto.user.UserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {

    // 로그인된 유저 정보 반환
    @GetMapping("/info")
    public UserInfoDTO getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return new UserInfoDTO(
                userDetails.getUser().getUserId(),
                userDetails.getUser().getUsername(),
                userDetails.getUser().getEmail(),
                userDetails.getUser().getRole()
        );
    }
}
