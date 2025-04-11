package kroryi.bus2.controller.api;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import kroryi.bus2.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;

    // 이메일 인증 코드 요청
    @PostMapping("/send-code")
    public ResponseEntity<String> sendCode(@RequestParam String email) {
        if (!isValidEmail(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효하지 않은 이메일 주소입니다.");
        }

        try {
            emailService.generateAndSendVerificationCode(email);  // 이메일 전송
            return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다.");
        } catch (IllegalStateException e) {
            log.error("인증 코드 전송 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("요청이 너무 자주 발생했습니다. 잠시 후 다시 시도해주세요.");
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이메일 전송 실패");
        }
    }

    // 인증 코드 검증
    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean verified = emailService.verifyCode(email, code);
        if (verified) {
            return ResponseEntity.ok("이메일 인증 성공");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 실패 또는 코드 만료");
        }
    }

    // 이메일 형식 검증
    private boolean isValidEmail(String email) {
        try {
            new InternetAddress(email).validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }
}
