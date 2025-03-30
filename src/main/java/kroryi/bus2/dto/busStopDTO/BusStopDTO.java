package kroryi.bus2.dto.busStopDTO;

import lombok.*;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusStopDTO implements Serializable {

   private String bsId;
   private String bsNm;
   private Double xPos;
   private Double yPos;

}