package kroryi.bus2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import kroryi.bus2.dto.BusRealtimeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusRouteRealTimeDataService {

    private final RestTemplate restTemplate;
    @Value("${api.bus.base-url}")
    private String baseUrl;

    @Value("${api.service-key-encoding}")
    private String encoding_serviceKey;


    public List<BusRealtimeDTO> getRealTimeBusList(String routeId) throws Exception {

        URI url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/getPos")
                .queryParam("serviceKey", encoding_serviceKey)
                .queryParam("routeId", routeId)
                .build(true)
                .toUri();
        System.out.println("url: " + url);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("API 호출 실패: " + response.getStatusCode());
        }

        System.out.println("response: " + response);

        ObjectMapper xmlMapper = new XmlMapper();
        JsonNode root = xmlMapper.readTree(response.getBody());

        // items는 여러 개 있을 수 있으니 배열로 처리
        JsonNode items = root.path("body").path("items");

        List<BusRealtimeDTO> result = new ArrayList<>();

        if (items.isArray()) {
            for (JsonNode item : items) {
                BusRealtimeDTO dto = BusRealtimeDTO.builder()
                        .routeId(item.path("routeId").asText())
                        .moveDir(item.path("moveDir").asInt())
                        .seq(item.path("seq").asInt())
                        .bsId(item.path("bsId").asText())
                        .xPos(item.path("xPos").asDouble())
                        .yPos(item.path("yPos").asDouble())
                        .routeNo(item.path("routeNo").asText())
                        .busTCd2(item.path("busTCd2").asText())
                        .vhcNo2(item.path("vhcNo2").asText())
                        .build();

                result.add(dto);
            }
        }
        return result;
    }

}
