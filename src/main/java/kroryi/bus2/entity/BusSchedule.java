package kroryi.bus2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "schedule")
public class BusSchedule {
    @Id
    private Long id;
    private int scheduleNo;

    private String schedule_A;
    private String schedule_B;
    private String schedule_C;
    private String schedule_D;
    private String schedule_E;
    private String schedule_F;
    private String schedule_G;
    private String schedule_H;

    private String routeId;
}
