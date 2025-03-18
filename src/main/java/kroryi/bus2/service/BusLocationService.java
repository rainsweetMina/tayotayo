package kroryi.bus2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import kroryi.bus2.repository.BusLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class BusLocationService {

//    public String fetchAndSaveBusLocation(String routeId) {
//        try {
//            String url = API_URL + "&routeId=" + routeId;
//            BusLocationResponse response = restTemplate.getForObject(url, BusLocationResponse.class);
//
//            if (response != null && response.getBody() != null && response.getBody().getItems() != null) {
//                List<BusLocation> stations = response.getBody().getItems().stream()
//                        .map(dto -> new BusLocation(
//                                dto.getBsId(),
//                                dto.getBsNm(),
//                                dto.getSeq(),
//                                dto.getMoveDir(),
//                                dto.getXPos(),
//                                dto.getYPos()
//                        ))
//                        .collect(Collectors.toList());
//
//                System.out.println("API 요청 URL: " + API_URL + "&routeId=" + routeId);
//                System.out.println("API 응답 데이터: " + response);
//                if (!stations.isEmpty()) {
//                    busLocationRepository.saveAll(stations);
//                    System.out.println("✅ Bus location data saved successfully");
//                } else {
//                    System.out.println("❌ 유효한 데이터 없음, 저장하지 않음");
//                }
//            } else {
//                System.out.println("❌ API 응답이 null이거나, body.items가 없음");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("❌ Error fetching bus locations");
//        }
//        return "데이터 삽입 완료";
//    }

    @Autowired
    private BusLocationRepository busLocationRepository;
//
//    @Autowired
//    private RestTemplate restTemplate;
//
////    private static final String API_URL = "https://apis.data.go.kr/6270000/dbmsapi01/getPos?serviceKey=hAVk7MvgXV8Uhq%2BCq90xSgZXr6s5dTjhoSTl%2BYiPvMH6%2FOqJQelUwLHedMvtXu9X92h6RpW19rhu6sVXIuwgJw%3D%3D&routeId=1000001000";
//    public String fetchAndSaveBusLocation() {
//        try {
//
//            String decodedKey = "hAVk7MvgXV8Uhq+Cq90xSgZXr6s5dTjhoSTl+YiPvMH6/OqJQelUwLHedMvtXu9X92h6RpW19rhu6sVXIuwgJw==";
//            String encodedKey = URLEncoder.encode(decodedKey, StandardCharsets.UTF_8);
//            String routeId = "1000001000";
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Accept", "*/*");  // 또는 headers.set("Accept", "application/json");
//
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//
//            String url = "http://apis.data.go.kr/6270000/dbmsapi01/getBs?serviceKey=" + encodedKey + "&routeId=" + routeId;
////            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
//            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
//            String jsonResponse = responseEntity.getBody();
//
//            System.out.println("🚀 API 요청 URL: " + url);
//            System.out.println("🔍 API 응답 데이터1: " + responseEntity);
//            System.out.println("🔍 API 응답 데이터2: " + jsonResponse);
//
//            // JSON을 Java 객체로 변환
//            ObjectMapper objectMapper = new ObjectMapper();
//            BusLocationResponse response = objectMapper.readValue(jsonResponse, BusLocationResponse.class);
//
//            // body나 items가 null인지 확인
//            if (response == null || response.getBody() == null || response.getBody().getItems() == null) {
//                System.out.println("❌ API 응답이 null이거나, body.items가 없음");
//                return "API 응답 오류";
//            }
//
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("❌ Error fetching bus locations");
//            return "API 요청 실패";
//        }
//        return "데이터 삽입 완료";
//    }

    @Autowired
    private RestTemplate restTemplate;

    String API_URL = "https://apis.data.go.kr/6270000/dbmsapi01/getBs?";
    String serviceKey = "j/gLHENNg0EDmUOP1OcG5WafUwAUq0u6D1CAZp7xdSTLsSmRJ7r6Pfi34Ks2ZZ7lM0zVZHjjESDToVIX+soPGA==";

    public String fetchAndSaveBusLocation(String routeId) {
        try {

            String APIurl = API_URL
                    + "serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8)  // URL 인코딩 필수
                    + "&routeId=" + routeId;

            URI uri = new URI(APIurl);
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(uri, String.class);
            System.out.println("url: " + uri);
            System.out.println(response);

            XmlMapper xmlMapper = new XmlMapper();
            JsonNode node = xmlMapper.readTree(response.getBytes());
            ObjectMapper jsonMapper = new ObjectMapper();
            String jsonResponse = jsonMapper.writeValueAsString(node);

            System.out.println("API 요청 URL: " + API_URL + "serviceKey=" + serviceKey + "&routeId=" + routeId);
            System.out.println("🔍 API 응답 responseEntity 데이터 : " + response);
            System.out.println("🔍 API 응답 jsonResponse 데이터: " + jsonResponse);

            return jsonResponse;


        } catch (Exception e) {
            System.out.println("❌ Error fetching bus locations: " + e.getMessage());
            return null;
        }
    }



//    public String getBusLocation(String routeId) {
//        String url = API_URL + "&routeId=" + routeId;
//
//        ResponseEntity<String> response = restTemplate.exchange(
//                url,
//                HttpMethod.GET,
//                null,
//                String.class
//        );
//
//        return response.getBody(); // API 응답 데이터를 그대로 반환
//    }
}
