package kroryi.bus2.dto;

import lombok.*;

import java.io.Serializable;

/**
 * DTO for {@link kroryi.bus2.entity.Link}
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LinkDTO implements Serializable {
    private Long id;
    private String linkId;
    private String linkNm;
    private String stNode;
    private String edNode;
    private Double gisDist;
}