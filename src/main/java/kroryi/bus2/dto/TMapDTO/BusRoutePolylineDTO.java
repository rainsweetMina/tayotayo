package kroryi.bus2.dto.TMapDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusRoutePolylineDTO {
    private String routeId;
    private List<LatLngDTO> polylineCoords; // 지도에 그릴 도로 기반 경로 좌표

}
