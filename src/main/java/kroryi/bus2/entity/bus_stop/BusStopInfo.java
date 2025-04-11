package kroryi.bus2.entity;

import jakarta.persistence.*;
import kroryi.bus2.entity.bus_stop.BusStop;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bus_stop_info")
public class BusStopInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bs_id")
    private String bsId; // BusStop의 bsId와 동일, PK로 사용

    @OneToOne
    @JoinColumn(name = "bs_id", referencedColumnName = "bs_id", insertable = false, updatable = false)
    private BusStop busStop; // 연결된 기본 정류장 엔티티

    @Column(name = "m_id")
    private String mId;

    @Column(name = "bs_nm_en")
    private String bsNmEn;

    @Column(name = "city")  // 시도
    private String city;

    @Column(name = "district")  // 구군
    private String district;

    @Column(name = "neighborhood")  // 동
    private String neighborhood;

    @Column(name = "route_count")
    private Integer routeCount;
}
