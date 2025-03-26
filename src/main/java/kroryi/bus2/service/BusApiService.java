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
public class BusApiService {

    private final WebClient webClient;

    @Value("${api.service-key}")
    private String serviceKey;

    public String getBusArrivalInfo(String bsId) {
        // 무슨 문제인지 밑에 uri 빌드가 디코딩된 키는 인코딩을 안하고 인코딩된 키는 이중 인코딩을 안함 일단 인코딩된 키를 넣었음
        String API_KEY = "j%2FgLHENNg0EDmUOP1OcG5WafUwAUq0u6D1CAZp7xdSTLsSmRJ7r6Pfi34Ks2ZZ7lM0zVZHjjESDToVIX%2BsoPGA%3D%3D";

        URI uri = UriComponentsBuilder
                .fromUriString("https://apis.data.go.kr/6270000/dbmsapi01/getRealtime")
                .queryParam("serviceKey", API_KEY)
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
