package kroryi.bus2.service.board;

import kroryi.bus2.entity.busStop.BusStopInfo;
import kroryi.bus2.repository.jpa.BusStopRepository;
import kroryi.bus2.repository.jpa.board.BusStopInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusStopInfoService {
    private final BusStopInfoRepository busStopInfoRepository;

}
