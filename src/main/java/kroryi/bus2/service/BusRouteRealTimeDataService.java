package kroryi.bus2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import kroryi.bus2.dto.BusRealtimeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class BusRouteRealTimeDataService {

    private final RestTemplate restTemplate;
    @Value("${api.bus.base-url}")
    private String baseUrl;

    @Value("${api.service-key-encoding}")
    private String encoding_serviceKey;


    public List<BusRealtimeDTO> getRealTimeBusList(String routeId) throws Exception {
        log.info("🚌 실시간 버스 위치 조회 시작 - routeId: {}", routeId);

        try {
            URI url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/getPos")
                    .queryParam("serviceKey", encoding_serviceKey)
                    .queryParam("routeId", routeId)
                    .build(true)
                    .toUri();
            log.info("📡 API 요청 URL: {}", url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.info("📥 API 응답 상태: {}", response.getStatusCode());
            
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("❌ API 호출 실패 - 상태 코드: {}, 응답: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("API 호출 실패: " + response.getStatusCode() + " - " + response.getBody());
            }

            String responseBody = response.getBody();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                log.warn("⚠️ API 응답이 비어있음 - routeId: {}", routeId);
                return new ArrayList<>();
            }

            log.info("📄 API 응답 본문 길이: {} 문자", responseBody.length());
            log.debug("📄 API 응답 본문: {}", responseBody);

            ObjectMapper xmlMapper = new XmlMapper();
            JsonNode root = xmlMapper.readTree(responseBody);
            log.info("✅ XML 파싱 성공");

            // items는 여러 개 있을 수 있으니 배열로 처리
            JsonNode items = root.path("body").path("items");
            log.info("📊 items 노드 타입: {}, 크기: {}", items.getNodeType(), items.size());

            List<BusRealtimeDTO> result = new ArrayList<>();

            if (items.isArray()) {
                for (JsonNode item : items) {
                    try {
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
                        log.debug("🚌 버스 정보 파싱 성공: {}", dto);
                    } catch (Exception e) {
                        log.error("❌ 개별 버스 정보 파싱 실패: {}", e.getMessage(), e);
                    }
                }
            } else {
                log.warn("⚠️ items가 배열이 아님 - 타입: {}", items.getNodeType());
            }

            log.info("✅ 실시간 버스 위치 조회 완료 - 총 {}개 버스", result.size());
            return result;

        } catch (RestClientException e) {
            log.error("❌ RestTemplate 오류 - routeId: {}, 오류: {}", routeId, e.getMessage(), e);
            throw new RuntimeException("외부 API 호출 중 오류 발생: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ 실시간 버스 위치 조회 중 예상치 못한 오류 - routeId: {}, 오류: {}", routeId, e.getMessage(), e);
            throw new RuntimeException("실시간 버스 위치 조회 실패: " + e.getMessage(), e);
        }
    }

}
