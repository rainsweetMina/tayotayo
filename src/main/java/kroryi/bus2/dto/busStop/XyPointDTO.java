package kroryi.bus2.dto.busStop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class XyPointDTO {
    private double xPos;  // 경도
    private double yPos;  // 위도
    private int moveDir; // ✅ 추가

}
