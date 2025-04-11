package kroryi.bus2.dto.board;

import lombok.Data;

import java.util.List;

@Data
public class BusCompanyDTO {
    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private List<String> companyRouteNo;
}
