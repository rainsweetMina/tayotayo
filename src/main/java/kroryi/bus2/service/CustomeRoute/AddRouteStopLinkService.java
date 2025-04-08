package kroryi.bus2.service.CustomeRoute;

import kroryi.bus2.dto.RouteStopLinkDTO;
import kroryi.bus2.entity.RouteStopLink;
import kroryi.bus2.repository.jpa.AddRouteStopLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddRouteStopLinkService {

    private final AddRouteStopLinkRepository repository;

    // 경유 정류장 리스트를 엔티티로 변환하여 일괄 저장
    public void saveAll(List<RouteStopLinkDTO> dtoList) {
        dtoList.forEach(dto -> {
            System.out.println("xPos = " + dto.getXPos() + ", yPos = " + dto.getYPos());
        });

        List<RouteStopLink> entities = dtoList.stream()
                .map(dto -> RouteStopLink.builder()
                        .routeId(dto.getRouteId())
                        .bsId(dto.getBsId())
                        .seq(dto.getSeq())
                        .moveDir(dto.getMoveDir())
                        .xPos(dto.getXPos())
                        .yPos(dto.getYPos())
                        .build()
                ).toList();

        repository.saveAll(entities);
    }
}