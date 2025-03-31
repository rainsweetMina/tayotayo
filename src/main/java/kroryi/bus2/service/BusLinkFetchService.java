package kroryi.bus2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import kroryi.bus2.dto.buslinkshapeDTO.BusLinkShapeDTO;
import kroryi.bus2.dto.coordinate.CoordinateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusLinkFetchService {

    private final ShapeRouteService shapeRouteService;
    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String LINK_API_URL = "https://apis.data.go.kr/6270000/dbmsapi01/getLink";

    @Value("${api.service-key-encoding}")
    private String encoding_serviceKey;

    @Value("${api.service-key-decoding}")
    private String decoding_serviceKey;

    @Value("${api.bus.base-url}")
    private String baseUrl;

    public List<BusLinkShapeDTO> getBusRouteLinksByRouteId(String routeId) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/getLink")
                    .queryParam("serviceKey", encoding_serviceKey)
                    .queryParam("routeId", routeId)
                    .build(true)
                    .toUri();

            System.out.println("[버스 경로 최종 요청 URI] " + uri);

            String response = restTemplate.getForObject(uri, String.class);
            System.out.println("📩 응답 본문:\n" + response);

            // ✅ XML → JsonNode로 파싱
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode root = xmlMapper.readTree(response);
            JsonNode items = root.path("body").path("items");

            if (items.isMissingNode() || !items.isArray()) {
                return new ArrayList<>();
            }

            List<BusLinkShapeDTO> result = new ArrayList<>();
            if (items.isArray()) {
                for (JsonNode item : items) {
                    String linkId = item.path("linkId").asText();
                    if (!linkId.isEmpty()) {
                        result.addAll(shapeRouteService.getLinkGeometryByLinkId(linkId));
                    }
                }
            } else if (items.has("linkId")) {
                // 단일 객체일 경우도 처리
                String linkId = items.path("linkId").asText();
                result.addAll(shapeRouteService.getLinkGeometryByLinkId(linkId));
            }


            return result;
        } catch (Exception e) {
            throw new RuntimeException("[노선 링크 조회 실패]", e);
        }
    }
}
