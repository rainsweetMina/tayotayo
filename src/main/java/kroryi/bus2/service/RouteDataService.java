package kroryi.bus2.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import kroryi.bus2.dto.LinkDTO;
import kroryi.bus2.dto.LinkResponse;
import kroryi.bus2.dto.LinkWithCoordDTO;
import kroryi.bus2.entity.Node;
import kroryi.bus2.entity.Route;
import kroryi.bus2.repository.NodeRepository;
import kroryi.bus2.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
// 버스 노선에 관한 서비스 클래스.
public class RouteDataService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RouteRepository routeRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final NodeRepository nodeRepository;

    @Value("${api.bus.base-url}")
    private String baseUrl;

    @Value("${api.service-key-encoding}")
    private String encoding_serviceKey;

    @Value("${api.service-key-decoding}")
    private String decoding_serviceKey;

    // 버스 노선명으로 검색
    private final long CACHE_EXPIRATION = 3600L;

    public List<Route> getBusByNm(String routeNo) {
        String redisKey = "bus:routeNo" + routeNo;

        // Redis에서 먼저 조회
        Object cache = redisTemplate.opsForValue().get(redisKey);
        if (cache != null) {
            System.out.println("[Redis Cache Hit] key: " + redisKey);
            return (List<Route>) cache;
        }

        // 레디스에 없으면 db에서 조회
        System.out.println("[Cache Miss] DB에서 조회 - routeNo: " + routeNo);
        List<Route> result = routeRepository.searchByRouteNumberFull(routeNo);
        System.out.printf("노선 result: %s\n", result);

        // 조회 결과 Redis에 저장 (15초 TTL)
        redisTemplate.opsForValue().set(redisKey, result, CACHE_EXPIRATION, TimeUnit.SECONDS);
        System.out.println("[Cache Store] Redis에 저장됨, TTL: " + CACHE_EXPIRATION + "초 (1시간)");

        return result;
    }

    public JsonNode getBusRoute(String routeId) throws IOException {
        String redisKey = "BUS_ROUTE::" + routeId;

        // ✅ Redis에 JSON 문자열로 저장하고, 꺼낼 때 다시 JsonNode로 파싱
        String cachedJson = (String) redisTemplate.opsForValue().get(redisKey);
        if (cachedJson != null) {
            System.out.println("✅ Redis에서 버스 경로 정보 캐시 불러옴: " + redisKey);
            ObjectMapper objectMapper = new ObjectMapper(); // ✅ JSON 파서로 변경
            return objectMapper.readTree(cachedJson);
        }

        System.out.println("📍 API 요청 routeId: " + routeId);

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/getBs")
                .queryParam("serviceKey", encoding_serviceKey)
                .queryParam("routeId", routeId)
                .build(true)
                .toUri();

        System.out.println("[버스 경로 최종 요청 URI] " + uri);

        String response = restTemplate.getForObject(uri, String.class);

        XmlMapper xmlMapper = new XmlMapper();
        JsonNode jsonNode = xmlMapper.readTree(response.getBytes());

        System.out.println("[XML → JSON] 변환 결과: " + jsonNode);

        // ✅ Redis에 문자열(JSON 형태)로 저장
        redisTemplate.opsForValue().set(redisKey, jsonNode.toString(), Duration.ofMinutes(10));
        System.out.println("📝 Redis에 버스 경로 정보 캐싱 완료: " + redisKey);

        return jsonNode;
    }

    public List<LinkDTO> getBusRouteLink(String routeId) throws IOException {
        String redisKey = "LINK::" + routeId;

        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (cached instanceof List<?>) {
            List<?> rawList = (List<?>) cached;

            if (!rawList.isEmpty() && rawList.get(0) instanceof LinkDTO) {
                List<LinkDTO> cachedLinks = (List<LinkDTO>) rawList;
                System.out.println("✅ Redis에서 링크 정보 캐시 불러옴: " + redisKey);
                return cachedLinks;
            }
        }

        System.out.println("📍 API 요청 routeId: " + routeId);

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/getLink")
                .queryParam("serviceKey", encoding_serviceKey)
                .queryParam("routeId", routeId)
                .build(true)
                .toUri();

        System.out.println("[버스 경로 링크 최종 요청 URI] " + uri);

        String xmlResponse = restTemplate.getForObject(uri, String.class);

        // 디버깅용 응답 출력
        System.out.println("📦 [받은 XML 응답 일부]");
        System.out.println(xmlResponse.substring(0, Math.min(500, xmlResponse.length())) + "...");

        // XML 전체를 DTO에 바로 매핑
        XmlMapper xmlMapper = new XmlMapper();
        LinkResponse dtoWrapper = xmlMapper.readValue(xmlResponse, LinkResponse.class);
        List<LinkDTO> items = dtoWrapper.getItems();

        // Redis에 캐싱 (예: 10분 동안)
        redisTemplate.opsForValue().set(redisKey, items, Duration.ofMinutes(10));
        System.out.println("📝 Redis에 링크 정보 캐싱 완료: " + redisKey);

        return items;
    }

    public List<LinkWithCoordDTO> getLinkWithCoordinates(List<LinkDTO> links) {
        List<LinkWithCoordDTO> result = new ArrayList<>();

        for (LinkDTO link : links) {
            String stId = link.getStNode();
            String edId = link.getEdNode();

            // Redis 캐시 확인
            String stKey = "NODE::" + stId;
            String edKey = "NODE::" + edId;

            Node stNode = (Node) redisTemplate.opsForValue().get(stKey);
            Node edNode = (Node) redisTemplate.opsForValue().get(edKey);

            // Redis에 없으면 DB에서 조회하고 캐싱
            if (stNode == null) {
                stNode = (Node) nodeRepository.findByNodeId(stId).orElse(null);
                if (stNode != null) {
                    redisTemplate.opsForValue().set(stKey, stNode, Duration.ofHours(1));
                }
            }
            if (edNode == null) {
                edNode = (Node) nodeRepository.findByNodeId(edId).orElse(null);
                if (edNode != null) {
                    redisTemplate.opsForValue().set(edKey, edNode, Duration.ofHours(1));
                }
            }

            // 노드가 하나라도 없으면 무시하고 로그 출력
            if (stNode == null || edNode == null) {
                System.out.printf("⚠️ 존재하지 않는 노드 있음 - stId: %s, edId: %s\n", stId, edId);
                continue;
            }

            result.add(LinkWithCoordDTO.builder()
                    .linkId(link.getLinkId())
                    .stNode(stId)
                    .edNode(edId)
                    .gisDist(link.getGisDist())
                    .stX(stNode.getXPos())
                    .stY(stNode.getYPos())
                    .edX(edNode.getXPos())
                    .edY(edNode.getYPos())
                    .moveDir(link.getMoveDir())
                    .build()
            );
        }

        return result;
    }


}
