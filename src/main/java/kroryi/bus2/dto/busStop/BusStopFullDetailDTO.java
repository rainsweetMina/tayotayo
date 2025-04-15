package kroryi.bus2.dto.busStop;

import kroryi.bus2.dto.Route.RouteIdAndNoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusStopFullDetailDTO {
    private String bsId;
    private String bsNm;
    private Double xPos;
    private Double yPos;

    // 시군동
    private String city;
    private String district;
    private String neighborhood;

    // 도착 노선 정보
    private List<RouteIdAndNoDTO> routes;
}
