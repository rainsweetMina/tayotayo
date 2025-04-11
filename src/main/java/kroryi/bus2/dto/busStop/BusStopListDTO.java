package kroryi.bus2.dto.busStop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusStopListDTO implements Serializable {
    private Long id;
    private String bsId;
    private String bsNm;
    private Double xpos;
    private Double ypos;
}