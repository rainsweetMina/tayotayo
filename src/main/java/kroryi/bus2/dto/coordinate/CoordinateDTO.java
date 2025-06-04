package kroryi.bus2.dto.coordinate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordinateDTO {
    @JsonProperty("xPos")
    private double xPos; // = longitude

    @JsonProperty("yPos")
    private double yPos; // = latitude

}
