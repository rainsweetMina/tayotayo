package kroryi.bus2.dto.ad;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdCompanyResponseDTO {
    private Long id;
    private String name;
    private String managerName;
    private String contactNumber;
    private String email;
}
