package kroryi.bus2.dto.ad;

import kroryi.bus2.entity.ad.AdCompany;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdCompanyDTO {
    private Long id;
    private String name;
    private String managerName;
    private String contactNumber;
    private String email;
}

