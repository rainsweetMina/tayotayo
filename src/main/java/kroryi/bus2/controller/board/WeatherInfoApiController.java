package kroryi.bus2.controller.board;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Hidden
@RestController
@RequestMapping("/api/public")
public class WeatherInfoApiController {
    @Value("${public.api-key}")
    private String apiKey;

    @GetMapping("/api-key")
    public Map<String, String> getApiKey() {
        return Map.of("apiKey", apiKey);
    }
}
