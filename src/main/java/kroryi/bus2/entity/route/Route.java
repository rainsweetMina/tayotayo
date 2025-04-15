package kroryi.bus2.entity.route;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "route")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_id", unique = true)
    private String routeId;

    @Column(name = "route_no")
    private String routeNo;

    @Column(name = "st_bs_id")
    private String stBsId;

    @Column(name = "ed_bs_id")
    private String edBsId;

    @Column(name = "st_nm")
    private String stNm;

    @Column(name = "ed_nm")
    private String edNm;

    @Column(name = "route_note", columnDefinition = "TEXT")
    private String routeNote;

    @Column(name = "dataconnareacd")
    private String dataconnareacd;

    @Column(name = "dir_route_note")
    private String dirRouteNote;

    @Column(name = "ndir_route_note")
    private String ndirRouteNote;

    @Column(name = "route_tCd")
    private String routeTCd;

}
