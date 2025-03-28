package kroryi.bus2.entity.board;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "schedule")
public class BusSchedule {
    @Id
    private double id;
    private int schedule_no;

    private String schedule_A;
    private String schedule_B;
    private String schedule_C;
    private String schedule_D;
    private String schedule_E;
    private String schedule_F;
    private String schedule_G;
    private String schedule_H;

    private String route_id;
}
