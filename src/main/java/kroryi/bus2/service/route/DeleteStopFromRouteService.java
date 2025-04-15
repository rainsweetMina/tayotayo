package kroryi.bus2.service.route;

import jakarta.transaction.Transactional;
import kroryi.bus2.entity.route.RouteStopLink;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class DeleteStopFromRouteService {

    private final RouteStopLinkRepository routeStopLinkRepository;

    @Transactional
    public void deleteStopFromRoute(String routeId, String moveDir, int seqToDelete) {
        // 1. 해당 정류소 찾기
        RouteStopLink stopToDelete = routeStopLinkRepository.findByRouteIdAndMoveDirAndSeq(routeId, moveDir, seqToDelete)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 정류소를 찾을 수 없습니다."));

        //  출발 정류소는 삭제 금지
        if ("1".equals(moveDir) && seqToDelete == 1) {
            throw new IllegalArgumentException("출발 정류소는 삭제할 수 없습니다.");
        }

        // 2. 정류소 삭제
        routeStopLinkRepository.delete(stopToDelete);

        // 3. 뒤의 정류소 seq -1 처리
        List<RouteStopLink> toUpdate = routeStopLinkRepository.findByRouteIdAndMoveDir(routeId, moveDir)
                .stream()
                .filter(stop -> stop.getSeq() > seqToDelete)
                .toList();

        toUpdate.forEach(stop -> stop.setSeq(stop.getSeq() - 1));
        routeStopLinkRepository.saveAll(toUpdate);
    }

}
