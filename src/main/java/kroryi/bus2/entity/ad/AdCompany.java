package kroryi.bus2.entity.ad;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;            // 회사명
    private String managerName;     // 담당자명
    private String contactNumber;   // 연락처
    private String email;           // 이메일 주소

    @Builder.Default
    private boolean deleted = false;

}

