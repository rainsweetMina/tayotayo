package kroryi.bus2.service.board;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kroryi.bus2.entity.BusScheduleHeader;
import kroryi.bus2.repository.jpa.board.BusScheduleHeaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BusScheduleHeaderService {

    private final BusScheduleHeaderRepository busScheduleHeaderRepository;
    private final ObjectMapper objectMapper;

    public void saveStopOrder(String routeId, String moveDir, List<Integer> stopOrder) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(stopOrder);

        BusScheduleHeader header = busScheduleHeaderRepository
                .findByRouteIdAndMoveDir(routeId, moveDir)
                .orElseGet(BusScheduleHeader::new);

        header.setRouteId(routeId);
        header.setMoveDir(moveDir);
        header.setStopOrder(json);

        busScheduleHeaderRepository.save(header);
    }

    public List<Integer> getStopOrder(String routeId, String moveDir) {
        try {
            Optional<BusScheduleHeader> optional;

            if (moveDir != null && !moveDir.isBlank()) {
                optional = busScheduleHeaderRepository.findByRouteIdAndMoveDir(routeId, moveDir);
            } else {
                List<BusScheduleHeader> list = busScheduleHeaderRepository.findByRouteId(routeId);
                if (!list.isEmpty()) {
                    // 여러 개 중 가장 최근거 혹은 첫 번째만 사용
                    optional = Optional.of(list.get(0));
                } else {
                    optional = Optional.empty();
                }
            }

            if (optional.isPresent()) {
                String json = optional.get().getStopOrder();
                Integer[] array = objectMapper.readValue(json, Integer[].class);
                return Arrays.asList(array);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
