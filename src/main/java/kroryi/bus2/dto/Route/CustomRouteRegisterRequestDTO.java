package kroryi.bus2.dto.Route;

import kroryi.bus2.dto.RouteStopLinkDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomRouteRegisterRequestDTO {
    private CustomRouteDTO route;
    private List<RouteStopLinkDTO> stopsForward;        // 정방향 경유지 목록
    private List<RouteStopLinkDTO> stopsBackward;       // 역방향 경유지 목록
}