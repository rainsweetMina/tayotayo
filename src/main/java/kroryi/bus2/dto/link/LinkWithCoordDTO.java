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
//    private double gisDist;

    private Integer moveDir;
    private double stX;
    private double stY;
    private double edX;
    private double edY;
}
