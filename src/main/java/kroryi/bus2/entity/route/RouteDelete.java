package kroryi.bus2.entity.route;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "route_delete") // 기존과 다르게 테이블명도 명시적으로 써줌
public class RouteDelete {

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

    // ✅ 추가된 컬럼
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
