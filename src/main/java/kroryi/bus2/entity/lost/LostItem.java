package kroryi.bus2.entity.lost;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import kroryi.bus2.entity.user.User;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // ✅ createdAt, updatedAt 자동 처리
public class LostItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;        // 분실물 제목
    private String content;      // 분실 내용
    private String busNumber;    // 버스 번호
    private String busCompany; // 버스회사


    private LocalDateTime lostTime; // 분실 시간

    @ManyToOne
    @JsonIgnore // ✅ 무한 순환 방지용
    private User reporter;       // 일반회원 정보 (User 테이블 참조)

    private boolean matched = false; // 매칭 여부
    private boolean visible = true;  // 관리자 숨김 여부
    private boolean deleted = false; // 소프트 삭제 여부 (✅ 추가됨)

    @CreatedDate
    private LocalDateTime createdAt; // 생성일시 (✅ 자동 입력)

    @LastModifiedDate
    private LocalDateTime updatedAt; // 수정일시 (✅ 자동 입력)
}

