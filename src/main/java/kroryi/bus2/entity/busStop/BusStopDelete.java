package kroryi.bus2.entity.busStop;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bus_stop_delete")
@Builder
public class BusStopDelete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bs_id")
    private String bsId;

    @Column(name = "bs_nm")
    private String bsNm;

    @Column(name = "x_pos")
    private Double xPos;

    @Column(name = "y_pos")
    private Double yPos;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}