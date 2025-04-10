package kroryi.bus2.dto.busStop;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BusStopDTO implements Serializable {

   private int seq;
   private String bsNm;
   private String bsId;
   private Double xPos;
   private Double yPos;

   public BusStopDTO() {}

   public BusStopDTO(String bsId, String bsNm, Double xPos, Double yPos) {
      this.bsId = bsId;
      this.bsNm = bsNm;
      this.xPos = xPos;
      this.yPos = yPos;
   }
}