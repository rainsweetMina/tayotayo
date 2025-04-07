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
    private List<RouteStopLinkDTO> stops;
}