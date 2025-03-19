package kroryi.bus2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link kroryi.bus2.entity.Node}
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NodeDTO implements Serializable {
    private Long id;
    private String nodeId;
    private String nodeNm;
    private Double xPos;
    private Double yPos;
    private String bsYn;
}