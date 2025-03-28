package kroryi.bus2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import kroryi.bus2.dto.BusStopDTO;
import kroryi.bus2.dto.RouteDTO;
import kroryi.bus2.entity.BusStop;
import kroryi.bus2.entity.Route;
import kroryi.bus2.repository.BusStopRepository;
import kroryi.bus2.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// 버스 노선에 관한 서비스 클래스.
public class RouteDataService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RouteRepository routeRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${api.bus.base-url}")
    private String baseUrl;

    @Value("${api.service-key-encoding}")
    private String encoding_serviceKey;

    @Value("${api.service-key-decoding}")
    private String decoding_serviceKey;

    // 버스 노선명으로 검색
    private final long CACHE_EXPIRATION = 3600L;
    public List<String> getBusByNm(String routeNo) {
        String redisKey = "bus:routeNo" + routeNo;

        // Redis에서 먼저 조회
        Object cache = redisTemplate.opsForValue().get(redisKey);
        if (cache != null) {
            System.out.println("[Redis Cache Hit] key: " + redisKey);
            return (List<String>) cache;
        }

        // 레디스에 없으면 db에서 조회
        System.out.println("[Cache Miss] DB에서 조회 - routeNo: " + routeNo);
        List<String> result = routeRepository.searchByRouteNumber(routeNo);

        // 조회 결과 Redis에 저장 (15초 TTL)
        redisTemplate.opsForValue().set(redisKey, result, CACHE_EXPIRATION, TimeUnit.SECONDS);
        System.out.println("[Cache Store] Redis에 저장됨, TTL: " + CACHE_EXPIRATION + "초 (1시간)");

        return result;
    }

    public List<JsonNode> getBusRoute(String routeNo) throws IOException {

        List<String> routeIds = routeRepository.findRouteIdsByRouteNo(routeNo);
        List<JsonNode> result = new ArrayList<>();
        XmlMapper xmlMapper = new XmlMapper(); // 매번 생성하지 않고 한 번만

        System.out.println("routeId: " + routeIds);

        for (String routeId : routeIds) {
            URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/getBs")
                    .queryParam("serviceKey", encoding_serviceKey) // 이미 인코딩된 키 사용
                    .queryParam("routeId", routeId)
                    .build(true) // 인코딩 적용
                    .toUri();

            System.out.println("[최종 요청 URI] " + uri);

            String response = restTemplate.getForObject(uri, String.class);
            JsonNode jsonNode = xmlMapper.readTree(response.getBytes());

            result.add(jsonNode);
        }
        System.out.println("[XML → JSON] 변환 결과: " + result);

        return result;
    }





}
