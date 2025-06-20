package kroryi.bus2.service.user;

import jakarta.mail.internet.MimeMessage;
import kroryi.bus2.model.EmailVerificationCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final Environment env;

    private final Map<String, EmailVerificationCode> verificationCodes = new ConcurrentHashMap<>();
    private final Set<String> verifiedEmails = ConcurrentHashMap.newKeySet();

    private String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public void sendVerificationCode(String rawEmail) {
        String email = normalize(rawEmail);
        log.debug("📨 [이메일 인증] 전송 요청 시작 - 대상: {}", email);

        try {
            String code = String.valueOf((int) (Math.random() * 900000) + 100000);
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(3);

            verificationCodes.put(email, new EmailVerificationCode(code, expiresAt));
            log.debug("✅ [이메일 인증] 코드 생성 - 이메일: {}, 코드: {}, 만료: {}", email, code, expiresAt);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("이메일 인증 코드");
            message.setText("인증 코드: " + code + "\n3분 내로 입력해주세요.");

            mailSender.send(message);
            log.info("📤 [이메일 인증] 이메일 전송 완료 - 대상: {}", email);
        } catch (Exception e) {
            log.error("❌ [이메일 인증] 전송 실패 - 대상: {}, 오류: {}", rawEmail, e.getMessage(), e);
            throw new EmailSendingException("이메일 전송에 실패했습니다.", e);
        }
    }

    public boolean verifyCode(String email, String code) {
        email = normalize(email);
        log.debug("🔍 [인증 검증] 시작 - 이메일: {}, 입력된 코드: {}", email, code);
        log.debug("📦 현재 verificationCodes: {}", verificationCodes.keySet());

        EmailVerificationCode stored = verificationCodes.get(email);

        if (stored == null) {
            log.warn("❌ [인증 검증] 실패 - 저장된 코드 없음 (이메일: {})", email);
            return false;
        }

        if (stored.isExpired()) {
            verificationCodes.remove(email);
            log.warn("⌛ [인증 검증] 실패 - 코드 만료 (이메일: {}, 만료시각: {})", email, stored.getExpiresAt());
            return false;
        }

        if (stored.getCode().equals(code)) {
            verificationCodes.remove(email);
            verifiedEmails.add(email);
            log.info("✅ [인증 검증] 성공 - 이메일: {}, 코드 일치", email);
            log.info("📌 인증 성공 후 verifiedEmails 목록: {}", verifiedEmails);
            return true;
        }

        log.warn("❌ [인증 검증] 실패 - 코드 불일치 (이메일: {}, 저장된: {}, 입력된: {})", email, stored.getCode(), code);
        return false;
    }

    public boolean isEmailVerified(String email) {
        email = normalize(email);
        boolean isVerified = verifiedEmails.contains(email);
        log.info("🔎 [인증 여부 확인] 이메일: {}, 인증 상태: {}, 현재 인증된 목록: {}", email, isVerified, verifiedEmails);
        return isVerified;
    }

    public void removeVerifiedEmail(String rawEmail) {
        String email = normalize(rawEmail);
        boolean removed = verifiedEmails.remove(email);
        log.info("🧹 [인증 상태 제거] 대상 이메일: {}, 제거 성공 여부: {}", email, removed);
    }

    public void generateAndSendVerificationCode(String rawEmail) {
        log.debug("🚀 이메일 인증 코드 생성 및 전송 요청: {}", rawEmail);
        sendVerificationCode(rawEmail);
    }

    public void sendTemporaryPassword(String rawEmail, String tempPassword) {
        String email = normalize(rawEmail);
        String subject = "비밀번호 재설정 안내";
        String body = """
            <h3>임시 비밀번호 안내</h3>
            <p>요청하신 계정의 임시 비밀번호는 다음과 같습니다:</p>
            <p><strong>%s</strong></p>
            <p>로그인 후 반드시 비밀번호를 변경해 주세요.</p>
        """.formatted(tempPassword);

        sendEmail(email, subject, body);
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            log.info("✅ 이메일 전송 성공: {}", to);
        } catch (Exception e) {
            log.error("❌ 이메일 전송 실패: {}", e.getMessage(), e);
            throw new EmailSendingException("이메일 전송 실패", e);
        }
    }

    public static class EmailSendingException extends RuntimeException {
        public EmailSendingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public void logVerifiedEmails() {
        log.info("🧾 현재 인증된 이메일 목록: {}", verifiedEmails);
    }
}
