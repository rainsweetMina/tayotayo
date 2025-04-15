package kroryi.bus2.service.board;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kroryi.bus2.dto.board.BusScheduleHeaderRequestDTO;
import kroryi.bus2.entity.BusScheduleHeader;
import kroryi.bus2.entity.RouteStopLink;
import kroryi.bus2.repository.jpa.board.BusScheduleHeaderRepository;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BusScheduleHeaderService {

    private final BusScheduleHeaderRepository busScheduleHeaderRepository;
    private final RouteStopLinkRepository routeStopLinkRepository;
    private final ObjectMapper objectMapper;

    // 웹 전용
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

    public Map<String, Object> getSeqsByRoute(String routeId) {
        List<String> seqList = routeStopLinkRepository.findSeqsByRouteId(routeId);
        return Map.of(
                "count : ", seqList.size(),
                "seqList : ", seqList
        );
    }

    public Map<String, Object> getSeqsByRouteIdAndMoveDir(String routeId, String moveDir) {
        List<String> seqList = routeStopLinkRepository.findSeqByRouteIdAndMoveDir(routeId, moveDir);
        return Map.of(
                "count", seqList.size(),
                "seqList", seqList,
                "moveDir", moveDir
        );
    }

    public List<String> findSeqsByRouteIdAndBusStopName(String routeId, String bsNm) {
        return routeStopLinkRepository.findSeqsByStopName(routeId, bsNm);
    }


    // CREATE
    public BusScheduleHeader create(BusScheduleHeaderRequestDTO dto) {
        List<Integer> validSeqList = routeStopLinkRepository
                .findSeqsByRouteIdAndMoveDir(dto.getRouteId(), dto.getMoveDir());

        validateStopOrder(dto.getStopOrder(), validSeqList); // ✅ 두 개 넘김

        BusScheduleHeader header = new BusScheduleHeader();
        header.setRouteId(dto.getRouteId());
        header.setMoveDir(dto.getMoveDir());
        header.setStopOrder(toJson(dto.getStopOrder()));

        return busScheduleHeaderRepository.save(header);
    }

    // READ ALL
    public List<BusScheduleHeader> findAll() {
        return busScheduleHeaderRepository.findAll();
    }

    // UPDATE
    public BusScheduleHeader update(int id, BusScheduleHeaderRequestDTO dto) {
        BusScheduleHeader header = busScheduleHeaderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID: " + id));

        List<Integer> validSeqList = routeStopLinkRepository
                .findSeqsByRouteIdAndMoveDir(dto.getRouteId(), dto.getMoveDir());

        validateStopOrder(dto.getStopOrder(), validSeqList);
        header.setRouteId(dto.getRouteId());
        header.setMoveDir(dto.getMoveDir());
        header.setStopOrder(toJson(dto.getStopOrder()));

        return busScheduleHeaderRepository.save(header);
    }

    // DELETE
    public void delete(int id) {
        busScheduleHeaderRepository.deleteById(id);
    }

    // 유틸
    private String toJson(List<Integer> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("stopOrder 변환 실패", e);
        }
    }

    private void validateStopOrder(List<Integer> stopOrder, List<Integer> validSeqFromRouteMap) {
        if (stopOrder == null || stopOrder.size() != 8) {
            throw new IllegalArgumentException("stopOrder : 반드시 8개(시작/중간6/종점)여야 합니다.");
        }

        int startSeq = Collections.min(validSeqFromRouteMap);
        int endSeq = Collections.max(validSeqFromRouteMap);

        if (!stopOrder.get(0).equals(startSeq)) {
            throw new IllegalArgumentException("시작 정류장이 올바르지 않습니다. seq=1이 있어야 합니다.");
        }

        if (!stopOrder.get(7).equals(endSeq)) {
            throw new IllegalArgumentException("종점 정류장이 올바르지 않습니다. 마지막 seq가 있어야 합니다.");
        }

        List<Integer> middle = stopOrder.subList(1, 7);
        Set<Integer> middleSet = new HashSet<>(middle);
        if (middleSet.size() < 6) {
            throw new IllegalArgumentException("중간 정류장 6개는 중복되지 않아야 합니다.");
        }

        // 검증: 모든 값이 노선 정류장 seq 목록에 존재하는가
        for (Integer seq : stopOrder) {
            if (!validSeqFromRouteMap.contains(seq)) {
                throw new IllegalArgumentException("존재하지 않는 정류장 seq: " + seq);
            }
        }

    }


}
