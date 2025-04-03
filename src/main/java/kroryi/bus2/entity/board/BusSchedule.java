package kroryi.bus2.entity.board;

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

    @Column(name = "route_id")
    private String routeId;

    @Column(name = "schedule_no")
    private int scheduleNo;

    @Column(name = "schedule_a")
    private String schedule_A;
    @Column(name = "schedule_b")
    private String schedule_B;
    @Column(name = "schedule_c")
    private String schedule_C;
    @Column(name = "schedule_d")
    private String schedule_D;
    @Column(name = "schedule_e")
    private String schedule_E;
    @Column(name = "schedule_f")
    private String schedule_F;
    @Column(name = "schedule_g")
    private String schedule_G;
    @Column(name = "schedule_h")
    private String schedule_H;
}
