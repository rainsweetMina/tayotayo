package kroryi.bus2.dto.board;

import lombok.Data;

import java.util.List;

@Data
public class BusScheduleHeaderRequestDTO {
    private String routeId;
    private String moveDir;
    private List<Integer> stopOrder;
}
