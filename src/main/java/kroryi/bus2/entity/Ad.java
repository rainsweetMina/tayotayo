package kroryi.bus2.entity;

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
    private String imageUrl;         // 광고 배너 이미지 경로
    private String linkUrl;          // 클릭 시 이동 링크 (선택)

    private LocalDateTime startDateTime;   // 광고 시작 시간
    private LocalDateTime endDateTime;     // 광고 종료 시간

    @Setter
    private boolean deleted;         // soft delete 여부

    public String getStatus() {
        LocalDateTime now = LocalDateTime.now();

        if (deleted) return "DELETED";
        if (now.isBefore(startDateTime)) return "SCHEDULED";            // 진행 전
        if (now.isAfter(endDateTime)) return "ENDED";                  // 종료됨
        if (now.plusDays(7).isAfter(endDateTime)) return "ENDING_SOON"; // 7일 이내 종료 예정

        return "ONGOING"; // 기본은 진행 중
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private AdCompany company;

}

