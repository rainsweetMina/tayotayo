package kroryi.bus2.controller.user;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import kroryi.bus2.service.user.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/email")
@Tag(name = "이메일-인증-API", description = "회원가입 시 이메일 인증 관련 API입니다.")
@Log4j2
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendCode(@RequestParam String email) {
        if (!isValidEmail(email)) {
            return ResponseEntity.badRequest().body("유효하지 않은 이메일 주소입니다.");
        }
        try {
            emailService.generateAndSendVerificationCode(email);
            return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다.");
        } catch (IllegalStateException e) {
            log.error("인증 코드 전송 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("요청이 너무 자주 발생했습니다. 잠시 후 다시 시도해주세요.");
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("이메일 전송 실패");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");

        try {
            boolean success = emailService.verifyCode(email, code);
            return ResponseEntity.ok(Map.of("success", success));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("🔴 이메일 인증 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "서버 오류"));
        }
    }

    // ✅ 클래스 안에 정확히 위치할 것
    private boolean isValidEmail(String email) {
        try {
            new InternetAddress(email).validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }
}