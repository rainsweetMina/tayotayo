package kroryi.bus2.entity.mypage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import kroryi.bus2.entity.busStop.BusStop;
import kroryi.bus2.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "favorite_bus_stop")
public class FavoriteBusStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bs_id")
    private String bsId;

    @Column(name = "user_id")
    private String userId;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bs_id", referencedColumnName = "bs_id", insertable = false, updatable = false)
    private BusStop busStop;  // 정류장 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;  // 해당 즐겨찾기를 한 사용자

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;  // 생성일시
}
