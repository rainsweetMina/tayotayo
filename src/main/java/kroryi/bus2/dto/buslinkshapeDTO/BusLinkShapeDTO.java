package kroryi.bus2.dto.buslinkshapeDTO;

import kroryi.bus2.dto.coordinate.CoordinateDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusLinkShapeDTO {
    private String linkId;
    private String routeId;
    private List<CoordinateDTO> coords;
}