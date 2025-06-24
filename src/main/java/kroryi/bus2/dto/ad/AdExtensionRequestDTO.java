// ✅ 1단계: 광고 연장 요청 DTO 생성
package kroryi.bus2.dto.ad;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdExtensionRequestDTO {
    private LocalDateTime newEndDateTime;
}
