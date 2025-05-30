package kroryi.bus2.service.busStop;

import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.dto.busStop.BusStopDetailResponseDTO;
import kroryi.bus2.entity.busStop.BusStop;
import kroryi.bus2.entity.busStop.BusStopInfo;
import kroryi.bus2.repository.jpa.board.BusStopInfoRepository;
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

        BusStop stop = BusStop.builder()
                .bsId(dto.getBsId())
                .bsNm(dto.getBsNm())
                .xPos(dto.getXPos())
                .yPos(dto.getYPos())
                .build();
        busStopRepository.save(stop);

        BusStopInfo info = BusStopInfo.builder()
                .bsId(dto.getBsId())
                .city(dto.getCity())
                .district(dto.getDistrict())
                .neighborhood(dto.getNeighborhood())
                .build();
        busStopInfoRepository.save(info);

        return stop;
    }

}
