package kroryi.bus2.service.busStop;

import jakarta.transaction.Transactional;
import kroryi.bus2.entity.busStop.BusStop;
import kroryi.bus2.entity.busStop.BusStopDelete;
import kroryi.bus2.entity.busStop.BusStopInfo;
import kroryi.bus2.entity.busStop.BusStopInfoDelete;
import kroryi.bus2.repository.jpa.board.BusStopInfoRepository;
import kroryi.bus2.repository.jpa.bus_stop.BusStopDeleteRepository;
import kroryi.bus2.repository.jpa.bus_stop.BusStopInfoDeleteRepository;
import kroryi.bus2.repository.jpa.bus_stop.BusStopRepository;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Log4j2
@RequiredArgsConstructor
public class DeleteBusStopService {

    private final BusStopRepository busStopRepository;
    private final RouteStopLinkRepository routeStopLinkRepository;
    private final BusStopInfoRepository busStopInfoRepository;
    private final BusStopDeleteRepository busStopDeleteRepository;
    private final BusStopInfoDeleteRepository busStopInfoDeleteRepository;

    @Transactional
    public void deleteBusStopIfNotLinked(String bsId) {
        // 1. 노선에 연결되어 있는지 확인
        int linkedCount = routeStopLinkRepository.countByBsId(bsId);

        if (linkedCount > 0) {
            throw new IllegalStateException("❌ 해당 정류장은 " + linkedCount + "개의 노선에 연결되어 있어 삭제할 수 없습니다.");
        }

        // 2. 정류장 상세정보 먼저 삭제
        busStopInfoRepository.deleteByBsId(bsId);
        // 3. 정류장 삭제
        busStopRepository.deleteByBsId(bsId);
    }

    @Transactional
    public void backupBusStop(String bsId) {
        // 기본 정류장 조회
        BusStop busStop = busStopRepository.findByBsId(bsId)
                .orElseThrow(() -> new IllegalArgumentException("정류장을 찾을 수 없습니다: " + bsId));

        // 정류장 정보 조회 (없을 수 있음)
        BusStopInfo busStopInfo = busStopInfoRepository.findByBsId(bsId);

        // 현재 시간
        LocalDateTime deletedAt = LocalDateTime.now();

        // 1. 정류장 백업 생성
        BusStopDelete busStopDelete = BusStopDelete.builder()
                .bsId(busStop.getBsId())
                .bsNm(busStop.getBsNm())
                .xPos(busStop.getXPos())
                .yPos(busStop.getYPos())
                .deletedAt(deletedAt)
                .build();

        busStopDeleteRepository.save(busStopDelete); // 저장

        // 2. 정류장 상세 정보 백업 (정보가 있는 경우에만)
        if (busStopInfo != null) {
            BusStopInfoDelete busStopInfoDelete = BusStopInfoDelete.builder()
                    .bsId(busStopInfo.getBsId())
                    .mId(busStopInfo.getMId())
                    .bsNmEn(busStopInfo.getBsNmEn())
                    .city(busStopInfo.getCity())
                    .district(busStopInfo.getDistrict())
                    .neighborhood(busStopInfo.getNeighborhood())
                    .routeCount(busStopInfo.getRouteCount())
                    .deletedAt(deletedAt)
                    .build();

            busStopInfoDeleteRepository.save(busStopInfoDelete);
        }
    }

}
