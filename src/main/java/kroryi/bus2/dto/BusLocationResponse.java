package kroryi.bus2.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BusLocationResponse {

    private Body body;

    public Body getBody() {
        return body;
    }
    public void setBody(Body body) {
        this.body = body;
    }
    public static class Body {
        private List<BusLocationDTO> items;
        public List<BusLocationDTO> getItems() {
            return items;
        }
        public void setItems(List<BusLocationDTO> items) {
            this.items = items;
        }
    }
}
