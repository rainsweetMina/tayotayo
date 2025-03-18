package kroryi.bus2.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "link")
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "link_id", unique = true)
    private String linkId;

    @Column(name = "link_nm")
    private String linkNm;

    @Column(name = "st_node")
    private String stNode;

    @Column(name = "ed_node")
    private String edNode;

    @Column(name = "gis_dist")
    private Double gisDist;
}
