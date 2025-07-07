package kroryi.bus2.dto.busStop;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusStopListDTO implements Serializable {
    private Long id;

    @JsonProperty("bsId")
    private String bsId;

    @JsonProperty("bsNm")
    private String bsNm;

    @JsonProperty("xPos")
    private double xPos;

    @JsonProperty("yPos")
    private double yPos;

    @JsonProperty("distance")
    private Double distance; // 현재 위치로부터의 거리 (미터)
}