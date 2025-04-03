package kroryi.bus2.BusDataInputTests;

import kroryi.bus2.entity.BusStop;
import kroryi.bus2.entity.Link;
import kroryi.bus2.entity.Node;
import kroryi.bus2.entity.Route;
import kroryi.bus2.repository.jpa.BusStopRepository;
import kroryi.bus2.repository.jpa.LinkRepository;
import kroryi.bus2.repository.jpa.NodeRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
@Log4j2
public class BusDataInputTest {

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private BusStopRepository busStopRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Test
    public void InsertBusData() throws IOException {
        // Node 테스트 데이터
        Node node = new Node();
        node.setNodeId("TEST_NODE_001");
        node.setNodeNm("테스트 정류장");
        node.setXPos(127.001);
        node.setYPos(37.001);
        node.setBsYn("Y");
        nodeRepository.save(node);

        // BusStop 테스트 데이터
        BusStop busStop = new BusStop();
        busStop.setBsId("TEST_BS_001");
        busStop.setBsNm("테스트 버스 정류장");
        busStop.setXPos(127.002);
        busStop.setYPos(37.002);
        busStopRepository.save(busStop);

        // Route 테스트 데이터
        Route route = new Route();
        route.setRouteId("TEST_ROUTE_001");
        route.setRouteNo("100");
        route.setStBsId("TEST_BS_001");
        route.setEdBsId("TEST_BS_002");
        route.setStNm("출발지");
        route.setEdNm("도착지");
        route.setRouteNote("테스트 노선입니다.");
        routeRepository.save(route);

        // Link 테스트 데이터
        Link link = new Link();
        link.setLinkId("TEST_LINK_001");
        link.setLinkNm("테스트 링크");
        link.setStNode("TEST_NODE_001");
        link.setEdNode("TEST_NODE_002");
        link.setGisDist(500.0);
        linkRepository.save(link);
    }
}
