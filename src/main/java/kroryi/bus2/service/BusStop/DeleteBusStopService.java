package kroryi.bus2.service.BusStop;

import jakarta.transaction.Transactional;
import kroryi.bus2.repository.jpa.BusStopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class DeleteBusStopService {

    private final BusStopRepository busStopRepository;

    @Transactional
    public void deleteBusStopIfNotLinked(String bsId) {
        int linkedCount = busStopRepository.countByBsId(bsId);

        if (linkedCount > 0) {
            throw new IllegalStateException("❌ 해당 정류장은 " + linkedCount + "개의 노선에 연결되어 있어 삭제할 수 없습니다.");
        }

        busStopRepository.deleteByBsId(bsId);
    }

}
