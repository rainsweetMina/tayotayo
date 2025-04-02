package kroryi.bus2.dto.link;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LinkWithCoordDTO {
//    private String linkId;
//    private String stNode;
//    private String edNode;
//    private Double gisDist;

    private Integer moveDir;
    private Double stX;
    private Double stY;
    private Double edX;
    private Double edY;
}
