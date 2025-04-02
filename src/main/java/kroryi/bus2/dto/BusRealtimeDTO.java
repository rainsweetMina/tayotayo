package kroryi.bus2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BusRealtimeDTO {
    private String routeId;
    private int moveDir;
    private int seq;
    private String bsId;
    private double xPos;
    private double yPos;
    private String routeNo;
    private String busTCd2;
    private String vhcNo2;
}
