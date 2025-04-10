package kroryi.bus2.dto.board;

import lombok.Data;

@Data
public class BusFareDTO {
    private String busType;
    private String payType;
    private Integer fareAdult;
    private Integer fareTeen;
    private Integer fareChild;

}
