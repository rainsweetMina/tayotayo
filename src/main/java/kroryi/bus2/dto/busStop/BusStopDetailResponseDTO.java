package kroryi.bus2.dto.busStop;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusStopDetailResponseDTO  {
    private String bsId;
    private String bsNm;
    private Double xPos;
    private Double yPos;

    // BusStopInfo에서 가져올 시/구/동 정보
    private String city;
    private String district;
    private String neighborhood;
}