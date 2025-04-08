package kroryi.bus2.dto.ad;

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

