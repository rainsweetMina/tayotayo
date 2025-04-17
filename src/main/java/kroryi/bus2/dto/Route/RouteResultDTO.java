package kroryi.bus2.dto.Route;

import kroryi.bus2.dto.busStop.BusStopDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResultDTO {
    private String type;          // "직통"
    private String routeId;
    private String routeNo;
    private String startBsId;
    private String endBsId;
    private int transferCount;    // 0
    private String transferStationId;
    private String transferStationName;
    private double estimatedMinutes;

    private List<BusStopDTO> stationIds;  // 🚨 추가됨: 출발~도착 정류장까지의 경유지 리스트

}