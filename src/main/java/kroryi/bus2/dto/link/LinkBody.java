package kroryi.bus2.dto.link;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkBody {

    @JacksonXmlProperty(localName = "items")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<LinkDTO> items;

    @JacksonXmlProperty(localName = "totalCount")
    @JsonIgnore
    private Integer totalCount;
}
