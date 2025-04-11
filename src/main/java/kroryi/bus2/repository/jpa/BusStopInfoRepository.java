package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.BusStopInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusStopInfoRepository extends JpaRepository<BusStopInfo, Integer> {

    BusStopInfo findByBsId(String bsId);

    void deleteByBsId(String bsId);
}
