package kroryi.bus2.dto.coordinate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordinateListWrapperDTO {
    private List<CoordinateDTO> coordinates;
}