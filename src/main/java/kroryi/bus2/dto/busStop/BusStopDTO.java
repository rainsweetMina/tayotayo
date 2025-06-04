package kroryi.bus2.dto.busStop;

import kroryi.bus2.entity.busStop.BusStop;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class BusStopDTO implements Serializable {

   private int seq;
   private String bsNm;
   private String bsId;
   private double xPos;
   private double yPos;

   public BusStopDTO() {}

   public BusStopDTO(String bsId, String bsNm, double xPos, double yPos) {
      this.bsId = bsId;
      this.bsNm = bsNm;
      this.xPos = xPos;
      this.yPos = yPos;
   }

   public BusStopDTO(int seq, String bsNm, String bsId, double xPos, double yPos) {
      this.seq = seq;
      this.bsNm = bsNm;
      this.bsId = bsId;
      this.xPos = xPos;
      this.yPos = yPos;
   }

   public static BusStopDTO fromEntity(BusStop entity) {
      return BusStopDTO.builder()
              .bsId(entity.getBsId())
              .bsNm(entity.getBsNm())
              .xPos(entity.getXPos())
              .yPos(entity.getYPos())
              .build();
   }
}