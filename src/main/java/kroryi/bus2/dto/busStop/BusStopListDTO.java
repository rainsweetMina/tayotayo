package kroryi.bus2.dto.busStop;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
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
    
    // 기본 생성자
    @Builder
    public BusStopListDTO(Long id, String bsId, String bsNm, double xPos, double yPos) {
        this.id = id;
        this.bsId = bsId;
        this.bsNm = bsNm;
        this.xPos = xPos;
        this.yPos = yPos;
        this.distance = null; // 거리 정보 없음
    }
    
    // 거리 정보 포함 생성자
    public BusStopListDTO(Long id, String bsId, String bsNm, double xPos, double yPos, double distance) {
        this.id = id;
        this.bsId = bsId;
        this.bsNm = bsNm;
        this.xPos = xPos;
        this.yPos = yPos;
        this.distance = distance;
    }
}