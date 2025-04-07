package kroryi.bus2.dto.ad;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdCompanyUpdateRequestDTO {
    private String name;
    private String managerName;
    private String contactNumber;
    private String email;
}

