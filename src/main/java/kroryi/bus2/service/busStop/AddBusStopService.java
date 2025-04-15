package kroryi.bus2.service.busStop;

import kroryi.bus2.dto.busStop.BusStopDetailResponseDTO;
import kroryi.bus2.entity.BusStopInfo;
import kroryi.bus2.entity.bus_stop.BusStop;
import kroryi.bus2.repository.jpa.bus_stop.BusStopInfoRepository;
import kroryi.bus2.repository.jpa.bus_stop.BusStopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class AddBusStopService {

    private final BusStopRepository busStopRepository;
    private final BusStopInfoRepository busStopInfoRepository;

    public BusStop createBusStop(BusStopDetailResponseDTO dto) {
        if (busStopRepository.existsByBsId(dto.getBsId())) {
            throw new IllegalArgumentException("이미 존재하는 정류장 ID입니다.");
        }

        // 정류장 기본 정보 저장
        BusStop stop = BusStop.builder()
                .bsId(dto.getBsId())
                .bsNm(dto.getBsNm())
                .xPos(dto.getXPos())
                .yPos(dto.getYPos())
                .build();

        BusStop savedStop = busStopRepository.save(stop);

        // 시/군/동 정보가 존재할 경우, BusStopInfo에도 저장
        if (dto.getCity() != null || dto.getDistrict() != null || dto.getNeighborhood() != null) {
            BusStopInfo info = BusStopInfo.builder()
                    .bsId(dto.getBsId())
                    .city(dto.getCity())
                    .district(dto.getDistrict())
                    .neighborhood(dto.getNeighborhood())
                    .build();
            busStopInfoRepository.save(info);
        }

        return savedStop;
    }

}
