package kroryi.bus2.dto.board;

import kroryi.bus2.entity.BusSchedule;
import lombok.Data;

@Data
public class BusScheduleDTO {
    private String routeId;
    private Integer scheduleNo;
    private String moveDir;
    private String busTCd;
    private String schedule_A;
    private String schedule_B;
    private String schedule_C;
    private String schedule_D;
    private String schedule_E;
    private String schedule_F;
    private String schedule_G;
    private String schedule_H;

    public BusSchedule toEntity(){
        return BusSchedule.builder()
                .routeId(routeId)
                .scheduleNo(scheduleNo)
                .moveDir(moveDir)
                .busTCd(busTCd)
                .schedule_A(schedule_A)
                .schedule_B(schedule_B)
                .schedule_C(schedule_C)
                .schedule_D(schedule_D)
                .schedule_E(schedule_E)
                .schedule_F(schedule_F)
                .schedule_G(schedule_G)
                .schedule_H(schedule_H)
                .build();
    }
}
