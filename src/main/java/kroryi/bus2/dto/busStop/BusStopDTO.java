package kroryi.bus2.dto.busStop;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusStopDTO implements Serializable {

   private int seq;
   private String bsNm;
   private String bsId;
   private Double xPos;
   private Double yPos;

}