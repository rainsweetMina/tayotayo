package kroryi.bus2.service.BusStop;

import kroryi.bus2.dto.busStop.BusStopUpdateDTO;
import kroryi.bus2.entity.busStop.BusStop;
import kroryi.bus2.repository.jpa.BusStopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class UpdateBusStopService {

    private final BusStopRepository busStopRepository;

    // BusStopService.java
    public void updateBusStop(String bsId, BusStopUpdateDTO dto) {
        BusStop stop = busStopRepository.findByBsId(bsId)
                .orElseThrow(() -> new RuntimeException("해당 bsId의 정류장이 존재하지 않습니다: " + bsId));

        stop.setBsNm(dto.getBsNm());
        stop.setXPos(dto.getXPos());
        stop.setYPos(dto.getYPos());

        busStopRepository.save(stop);
    }
}
