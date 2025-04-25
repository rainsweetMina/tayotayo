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

@Hidden
@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/user/email")
@Tag(name = "이메일-인증-API", description = "회원가입 시 이메일 인증 관련 API입니다.")
public class EmailController {

    private final EmailService emailService;

    @Operation(summary = "이메일 인증 코드 전송", description = "입력한 이메일로 인증 코드를 전송합니다.")
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

    @Operation(summary = "이메일 인증 코드 검증", description = "입력한 이메일 주소와 인증 코드를 검증하여 유효한지 확인합니다.")
    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean verified = emailService.verifyCode(email, code);
        if (verified) {
            return ResponseEntity.ok("이메일 인증 성공");
        } else {
            return ResponseEntity.badRequest().body("인증 실패 또는 코드 만료");
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

    @RestController
    @RequestMapping
    public class MailTestController {

        private final EmailService emailService;

        public MailTestController(EmailService emailService) {
            this.emailService = emailService;
        }

        @GetMapping("/mail-test")
        public ResponseEntity<String> sendTestEmail(@RequestParam String email) {
            emailService.generateAndSendVerificationCode(email);
            return ResponseEntity.ok("메일 전송 성공!");
        }
    }


}
