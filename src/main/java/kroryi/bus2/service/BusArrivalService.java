package kroryi.bus2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@Log4j2
@RequiredArgsConstructor
// 버스 도착 정보를 외부 공공데이터 API를 통해 조회하는 서비스 클래스
public class BusArrivalService {

    private final WebClient webClient;

    @Value("${api.service-key-decoding}")
    private String Decoding_serviceKey;

    @Value("${api.service-key-encoding}")
    private String encoding_serviceKey;

    @Value("${api.bus.base-url}")
    private String baseUrl;


    public String getBusArrivalInfo(String bsId) {

        // 무슨 문제인지 밑에 uri 빌드가 디코딩된 키는 인코딩을 안하고 인코딩된 키는 이중 인코딩을 안함 일단 인코딩된 키를 넣었음
        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/getRealtime")
                .queryParam("serviceKey", encoding_serviceKey)
                .queryParam("bsId", bsId)
                .build(true) // ✅ 자동 인코딩
                .toUri();

        log.info("📡 최종 요청 URI: {}", uri);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }



}
