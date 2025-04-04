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

    public void saveStopOrder(String routeId, List<String> stopOrder) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(stopOrder);

        BusScheduleHeader header = busScheduleHeaderRepository.findByRouteId(routeId)
                .orElseGet(BusScheduleHeader::new);

        header.setRouteId(routeId);
        header.setStopOrder(json);

        busScheduleHeaderRepository.save(header);
    }

    public List<String> getStopOrder(String routeId) {
        Optional<BusScheduleHeader> header = busScheduleHeaderRepository.findByRouteId(routeId);

        if (header.isPresent()) {
            try {
                String[] array = objectMapper.readValue(header.get().getStopOrder(), String[].class);
                return Arrays.asList(array);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }
}
