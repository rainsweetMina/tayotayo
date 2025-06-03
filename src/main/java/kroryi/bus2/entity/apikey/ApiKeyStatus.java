package kroryi.bus2.entity.apikey;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ApiKeyStatus {
    PENDING, // 승인 대기
    APPROVED, // 승인
    EXPIRED // 만료
}
