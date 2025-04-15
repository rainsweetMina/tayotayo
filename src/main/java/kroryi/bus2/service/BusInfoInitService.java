package kroryi.bus2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.transaction.Transactional;
import kroryi.bus2.entity.*;

import kroryi.bus2.entity.bus_stop.BusStop;
import kroryi.bus2.entity.route.Route;
import kroryi.bus2.repository.jpa.bus_stop.BusStopRepository;
import kroryi.bus2.repository.jpa.LinkRepository;
import kroryi.bus2.repository.jpa.NodeRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
@Log4j2
// 노드, 정류장, 노선, 링크 등의 버스 기초 정보를 공공 API로부터 조회하여 DB에 저장하는 서비스 클래스
public class BusInfoInitService {

    private final BusStopRepository busStopRepository;
    private final NodeRepository nodeRepository;
    private final RouteRepository routeRepository;
    private final LinkRepository linkRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final DataSource dataSource;



    public JsonNode getBusStopNav(String apiUrl) {

        try {
            URI uri = new URI(apiUrl);
            String response = restTemplate.getForObject(uri, String.class);

            XmlMapper xmlMapper = new XmlMapper();
            JsonNode node = xmlMapper.readTree(response.getBytes());
            ObjectMapper jsonMapper = new ObjectMapper();
            String jsonResponse = jsonMapper.writeValueAsString(node);
            JsonNode jsonNode = jsonMapper.readTree(jsonResponse);

            log.info("데이터 : {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
//            System.out.println("데이터 확인 : " + jsonNode);

            return jsonNode;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 연결확인용인듯 혹시 모르니 냅둬볼게요
    public void checkConnection() {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("DB 연결 상태: " + !connection.isClosed());
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            System.out.println("변환된 JSON 데이터: " + jsonNode.toPrettyString());

            JsonNode nodeData = jsonNode.path("body").path("items").path("node");
            System.out.println("nodeData 구조: " + nodeData);
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

            try {
                nodeRepository.save(newNode);
                System.out.println("Node 저장 성공: " + newNode.getNodeId());
            } catch (Exception e) {
                System.out.println("Node 저장 실패: " + e.getMessage());
            }


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
