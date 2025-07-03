package kroryi.bus2.dto.lowfloorbus;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLowFloorBusRequestDTO {
    @NotBlank
    private String title;

    @NotBlank
    private String content;
    
    private boolean topNotice;
} 