package kroryi.bus2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class BusApiService {

    private final WebClient webClient;


    public String getBusArrivalInfo(String busStopId) {
        String API_KEY = "oDPMcPKGx7dsFyVw5YzReqSK07UuJoUrABe2dbwM7zt9yVfOjSlE7SQtdIir+EW+DWAcIvio0lm1rR2sMnW7iw==";
        return webClient.get()
                .uri( "/arrival?busStopId=" + busStopId + "&serviceKey=" + API_KEY)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }


}
