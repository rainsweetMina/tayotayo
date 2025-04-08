package kroryi.bus2.model;

import java.time.LocalDateTime;

public class EmailVerificationCode {
    private final String code;
    private final LocalDateTime expiresAt;

    public EmailVerificationCode(String code, LocalDateTime expiresAt) {
        this.code = code;
        this.expiresAt = expiresAt;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    // 인증 코드가 만료되었는지 확인하는 메서드
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // 디버깅 용도로 코드와 만료시간을 문자열로 출력하도록 할 수 있습니다.
    @Override
    public String toString() {
        return "EmailVerificationCode{code='" + code + "', expiresAt=" + expiresAt + "}";
    }

    // equals와 hashCode 메서드를 추가하여 객체 비교가 필요할 경우 유용하게 활용할 수 있습니다.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EmailVerificationCode that = (EmailVerificationCode) obj;
        return code.equals(that.code) && expiresAt.equals(that.expiresAt);
    }

    @Override
    public int hashCode() {
        return 31 * code.hashCode() + expiresAt.hashCode();
    }
}
