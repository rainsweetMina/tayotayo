package kroryi.bus2.entity.ad;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;            // 광고 제목
    @Column(columnDefinition = "TEXT")
    private String imageUrl;
    // 광고 배너 이미지 경로
    private String linkUrl;          // 클릭 시 이동 링크 (선택)

    private LocalDateTime startDateTime;   // 광고 시작 시간
    private LocalDateTime endDateTime;     // 광고 종료 시간

    @Setter
    private boolean deleted;         // soft delete 여부

    public String getStatus() {
        LocalDateTime now = LocalDateTime.now();

        // ✅ null 방어 코드 추가
        if (startDateTime == null || endDateTime == null) return "UNKNOWN";

        if (deleted) return "DELETED";
        if (now.isBefore(startDateTime)) return "SCHEDULED";
        if (now.isAfter(endDateTime)) return "ENDED";
        if (now.plusDays(7).isAfter(endDateTime)) return "ENDING_SOON";

        return "ONGOING";
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private AdCompany company;

    @Column(nullable = false)
    private boolean showPopup;


}

