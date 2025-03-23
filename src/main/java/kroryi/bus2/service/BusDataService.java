package kroryi.bus2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.transaction.Transactional;
import kroryi.bus2.dto.BusStopDTO;
import kroryi.bus2.entity.BusStop;
import kroryi.bus2.entity.Link;
import kroryi.bus2.entity.Node;
import kroryi.bus2.entity.Route;
import kroryi.bus2.repository.BusStopRepository;
import kroryi.bus2.repository.LinkRepository;
import kroryi.bus2.repository.NodeRepository;
import kroryi.bus2.repository.RouteRepository;
import kroryi.bus2.util.FakeRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class BusDataService {

    private final NodeRepository nodeRepository;
    private final BusStopRepository busStopRepository;
    private final RouteRepository routeRepository;
    private final LinkRepository linkRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final FakeRedis fakeRedis;


    public JsonNode getBusStopNav(String apiUrl) {
        try {
            // 1. 캐시 먼저 확인
            String key = "nav:" + apiUrl;
            Object cached = fakeRedis.get(key);
            if (cached != null) {
                return (JsonNode) cached;
            }

            // 2. API 호출
            URI uri = new URI(apiUrl);
            String response = restTemplate.getForObject(uri, String.class);

            // 3. XML → JSON 변환
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode node = xmlMapper.readTree(response.getBytes());
            ObjectMapper jsonMapper = new ObjectMapper();
            String jsonResponse = jsonMapper.writeValueAsString(node);
            JsonNode jsonNode = jsonMapper.readTree(jsonResponse);

            // 4. 로그 출력
            log.info("데이터 : {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));

            // 5. 캐시에 저장 (30초)
            fakeRedis.setWithTTL(key, jsonNode, 30);

            return jsonNode;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 기초 정보 데이터 db에 넣는거
    @Transactional
    public void fetchAndSaveBusData(String apiUrl) {
        try {
            // API 호출
            URI uri = new URI(apiUrl);
            String response = restTemplate.getForObject(uri, String.class);

            XmlMapper xmlMapper = new XmlMapper();
            JsonNode node = xmlMapper.readTree(response.getBytes());
            ObjectMapper jsonMapper = new ObjectMapper();
            String jsonResponse = jsonMapper.writeValueAsString(node);
            JsonNode jsonNode = jsonMapper.readTree(jsonResponse);

            JsonNode nodeData = jsonNode.path("body").path("items").path("node");
            JsonNode bsData = jsonNode.path("body").path("items").path("bs");
            JsonNode routeData = jsonNode.path("body").path("items").path("route");
            JsonNode linkData = jsonNode.path("body").path("items").path("link");

            System.out.println("추출된 node 데이터: " + nodeData);

            if (nodeData.isArray() && !nodeData.isEmpty()) {
                saveNodes(nodeData);
            } else {
                System.out.println("nodeData 데이터가 없음!");
            }

            if (bsData.isArray() && !bsData.isEmpty()) {
                saveBusStops(bsData);
            } else {
                System.out.println("bsData 데이터가 없음!");
            }

            if (routeData.isArray() && !routeData.isEmpty()) {
                saveRoutes(routeData);
            } else {
                System.out.println("routeData 데이터가 없음!");
            }

            if (linkData.isArray() && !linkData.isEmpty()) {
                saveLinks(linkData);
            } else {
                System.out.println("linkData 데이터가 없음!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void saveNodes(JsonNode nodeArray) {
        for (JsonNode node : nodeArray) {
            Node newNode = new Node();
            newNode.setNodeId(node.path("nodeId").asText());
            newNode.setNodeNm(node.path("nodeNm").asText());
            newNode.setXPos(node.path("xPos").asDouble());
            newNode.setYPos(node.path("yPos").asDouble());
            newNode.setBsYn(node.path("bsYn").asText());

            System.out.println("저장될 Node 데이터: " + newNode);
            nodeRepository.save(newNode);
        }
    }
    private void saveBusStops(JsonNode busArray) {
        for (JsonNode bus : busArray) {
            BusStop newBusStop = new BusStop();
            newBusStop.setBsId(bus.path("bsId").asText());
            newBusStop.setBsNm(bus.path("bsNm").asText());
            newBusStop.setXPos(bus.path("xPos").asDouble());
            newBusStop.setYPos(bus.path("yPos").asDouble());

            System.out.println("저장될 Node 데이터: " + newBusStop);
            busStopRepository.save(newBusStop);
        }
    }
    private void saveRoutes(JsonNode routeArray) {
        for (JsonNode route : routeArray) {
            Route newRoute = new Route();
            newRoute.setRouteId(route.path("routeId").asText());
            newRoute.setRouteNo(route.path("routeNo").asText());
            newRoute.setStBsId(route.path("stBsId").asText());
            newRoute.setEdBsId(route.path("edBsId").asText());
            newRoute.setStNm(route.path("stNm").asText());
            newRoute.setEdNm(route.path("edNm").asText());
            newRoute.setRouteNote(route.path("routeNote").asText());

            System.out.println("저장될 Node 데이터: " + newRoute);
            routeRepository.save(newRoute);
        }
    }
    private void saveLinks(JsonNode linkArray) {
        for (JsonNode link : linkArray) {
            Link newLink = new Link();
            newLink.setLinkId(link.path("linkId").asText());
            newLink.setLinkNm(link.path("linkNm").asText());
            newLink.setStNode(link.path("stNode").asText());
            newLink.setEdNode(link.path("edNode").asText());
            newLink.setGisDist(link.path("gisDist").asDouble());

            System.out.println("저장될 Node 데이터: " + newLink);
            linkRepository.save(newLink);
        }
    }
}
