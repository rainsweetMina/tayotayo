package kroryi.bus2.dto.link;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.*;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "Result")
public class LinkResponse {

    @JacksonXmlProperty(localName = "body")
    private LinkBody body;

    public List<LinkDTO> getItems() {
        return body != null ? body.getItems() : Collections.emptyList();
    }
}
