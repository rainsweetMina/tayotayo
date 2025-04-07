package kroryi.bus2.entity.lost;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url; // 예: "/images/found/1234.jpg"

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "found_item_id")
    private FoundItem foundItem;
}

