package kroryi.bus2.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;

import java.io.Serializable;

/**
 * DTO for {@link kroryi.bus2.entity.Link}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LinkDTO implements Serializable {

    @JsonIgnore
    private Long id;

    @JsonIgnore
    private String linkNm;

    @JacksonXmlProperty(localName = "linkId")
    private String linkId;

    @JacksonXmlProperty(localName = "stNode")
    private String stNode;

    @JacksonXmlProperty(localName = "edNode")
    private String edNode;

    @JacksonXmlProperty(localName = "gisDist")
    private Double gisDist;

    @JacksonXmlProperty(localName = "linkSeq")
    private Integer linkSeq;

    @JacksonXmlProperty(localName = "moveDir")
//    @JsonIgnore // 🔥 JSON 직렬화/응답에선 숨기지만, 내부에서는 접근 가능
    private Integer moveDir;


}