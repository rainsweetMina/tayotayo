package kroryi.bus2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "schedule")
public class BusSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_id")
    private String routeId;

    @Column(name = "schedule_no")
    private int scheduleNo;

    @Column(name = "move_dir")
    private String moveDir;

    @Column(name = "bustcd")
    private String busTCd;

    @Column(name = "schedule_A")
    private String schedule_A;
    @Column(name = "schedule_B")
    private String schedule_B;
    @Column(name = "schedule_C")
    private String schedule_C;
    @Column(name = "schedule_D")
    private String schedule_D;
    @Column(name = "schedule_E")
    private String schedule_E;
    @Column(name = "schedule_F")
    private String schedule_F;
    @Column(name = "schedule_G")
    private String schedule_G;
    @Column(name = "schedule_H")
    private String schedule_H;


}
