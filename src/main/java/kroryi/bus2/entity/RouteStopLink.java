package kroryi.bus2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "route_stop_link")
public class RouteStopLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_id")
    private String routeId;

    @Column(name = "bs_id")
    private String bsId;

    @Column(name = "seq")
    private int seq;

    @Column(name = "move_dir")
    private String moveDir;

    @Column(name = "x_pos")
    private Double xPos;

    @Column(name = "y_pos")
    private Double yPos;

}
