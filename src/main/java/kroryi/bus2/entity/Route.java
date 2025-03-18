package kroryi.bus2.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
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
}
