package kroryi.bus2.dto.busSetting;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "PathSettingDTO")
public class PathSettingDTO {
    @JacksonXmlProperty(localName = "startDistance")
    private double startDistance;

    @JacksonXmlProperty(localName = "endDistance")
    private double endDistance;

    @JacksonXmlProperty(localName = "timeFactor")
    private double timeFactor;

}
