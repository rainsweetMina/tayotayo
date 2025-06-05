package kroryi.bus2.dto.Route;

import lombok.Data;

import java.util.List;

@Data
public class OrsRouteResponseDTO {
    private List<Route> routes;

    @Data
    public static class Route {
        private Summary summary;
        private String geometry;  // ORS에서 반환되는 encoded polyline
    }

    @Data
    public static class Summary {
        private double distance; // 단위: meters
        private double duration; // 단위: seconds
    }
}
