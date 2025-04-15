package kroryi.bus2.dto.busStop;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransferCandidate {
    private String bsId;
    private double totalDistance;
}