package kroryi.bus2.controller.board;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Hidden
@RestController
@RequestMapping("/api/public")
@Log4j2
public class WeatherInfoApiController {
    @Value("${public.api-key}")
    private String apiKey;

    @GetMapping("/api-key")
    public Map<String, String> getApiKey() {
        log.trace("getApiKey-------------> {}", apiKey);
        return Map.of("apiKey", apiKey);
    }
}
