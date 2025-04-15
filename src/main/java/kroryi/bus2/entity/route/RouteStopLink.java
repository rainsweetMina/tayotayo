package kroryi.bus2.entity.route;

import jakarta.persistence.*;
import kroryi.bus2.entity.bus_stop.BusStop;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
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

    // ✅ 정류소 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bs_id", referencedColumnName = "bs_id", insertable = false, updatable = false)
    private BusStop busStop;

}
