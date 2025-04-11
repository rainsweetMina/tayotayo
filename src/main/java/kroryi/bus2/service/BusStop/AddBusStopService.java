package kroryi.bus2.service.BusStop;

import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.entity.bus_stop.BusStop;
import kroryi.bus2.repository.jpa.BusStopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class AddBusStopService {

    private final BusStopRepository busStopRepository;

    public BusStop createBusStop(BusStopDTO dto) {
        if (busStopRepository.existsByBsId(dto.getBsId())) {
            throw new IllegalArgumentException("이미 존재하는 정류장 ID입니다.");
        }

        BusStop stop = new BusStop();
        stop.setBsId(dto.getBsId());
        stop.setBsNm(dto.getBsNm());
        stop.setXPos(dto.getXPos());
        stop.setYPos(dto.getYPos());

        return busStopRepository.save(stop);
    }
}
