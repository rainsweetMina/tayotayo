package kroryi.bus2.service.user;

import kroryi.bus2.model.EmailVerificationCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

    // 인증 코드 저장소
    private final Map<String, EmailVerificationCode> verificationCodes = new ConcurrentHashMap<>();
    private final Set<String> verifiedEmails = ConcurrentHashMap.newKeySet();

    /**
     * 이메일 인증 코드를 생성하고 전송합니다.
     * @param email 인증 코드를 받을 이메일 주소
     */
    public void sendVerificationCode(String email) {
        log.debug("이메일 인증 코드 전송 시작: {}", email);
        try {
            // 인증 코드 생성 (6자리 숫자)
            String code = String.valueOf((int) (Math.random() * 900000) + 100000);

            // 인증 코드 만료 시간 (3분)
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(3);

            // 이메일 인증 코드 저장
            verificationCodes.put(email, new EmailVerificationCode(code, expiresAt));

            // 이메일 메시지 생성
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("이메일 인증 코드");
            message.setText("인증 코드: " + code + "\n3분 내로 입력해주세요.");

            // 이메일 전송
            mailSender.send(message);
            log.info("이메일 전송 완료: {} -> 인증 코드 전송", email);
        } catch (Exception e) {
            log.error("이메일 전송 중 오류 발생: {}", e.getMessage(), e);
            throw new EmailSendingException("이메일 전송에 실패했습니다.", e); // EmailSendingException 예외 클래스를 만들어서 던지기
        }
    }

    /**
     * 인증 코드를 검증합니다.
     * @param email 인증 코드가 전송된 이메일 주소
     * @param code 사용자가 입력한 인증 코드
     * @return 인증 코드가 유효하면 true, 그렇지 않으면 false
     */
    public boolean verifyCode(String email, String code) {
        EmailVerificationCode stored = verificationCodes.get(email);

        // 이메일 인증 코드가 존재하지 않으면
        if (stored == null) {
            log.warn("인증 코드가 존재하지 않음: {}", email);
            return false;
        }

        // 인증 코드가 만료되었으면
        if (stored.isExpired()) {
            verificationCodes.remove(email);  // 만료된 인증 코드는 삭제
            log.warn("인증 코드가 만료되었습니다: {}", email);
            return false;
        }

        // 인증 코드가 맞으면
        if (stored.getCode().equals(code)) {
            verificationCodes.remove(email); // 1회성 코드 사용 후 삭제
            verifiedEmails.add(email);  // 인증 완료된 이메일 추가
            log.info("인증 코드 확인 완료: {}", email);
            return true;
        }

        log.warn("잘못된 인증 코드 입력: {}", email);
        return false;
    }

    /**
     * 이메일이 인증되었는지 확인합니다.
     * @param email 인증 여부를 확인할 이메일 주소
     * @return 이메일이 인증되었으면 true, 그렇지 않으면 false
     */
    public boolean isEmailVerified(String email) {
        boolean isVerified = verifiedEmails.contains(email);
        log.debug("이메일 인증 상태 확인: 이메일 = {}, 인증됨 = {}", email, isVerified);
        return isVerified;
    }

    /**
     * 인증 완료 후 이메일 인증 상태를 제거합니다.
     * @param email 인증 완료 후 상태를 제거할 이메일 주소
     */
    public void removeVerifiedEmail(String email) {
        verifiedEmails.remove(email);
        log.info("인증 완료 후 상태 제거: {}", email);
    }

    /**
     * 이메일 인증 코드를 생성하고 전송합니다.
     * @param email 인증 코드를 전송할 이메일 주소
     */
    public void generateAndSendVerificationCode(String email) {
        log.debug("이메일 인증 코드 생성 및 전송 시작: {}", email);
        sendVerificationCode(email);  // 이메일 인증 코드 생성 및 전송
    }

    // 이메일 전송 실패 시 던지는 예외 클래스
    public static class EmailSendingException extends RuntimeException {
        public EmailSendingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
