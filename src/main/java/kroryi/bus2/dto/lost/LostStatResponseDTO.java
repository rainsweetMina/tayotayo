package kroryi.bus2.dto.lost;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LostStatResponseDTO {
    private long reported;
    private long found;
    private long matched;
}

