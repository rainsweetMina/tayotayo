package kroryi.bus2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponseStatDTO {
    private String date;
    private Double averageResponseTime;
}