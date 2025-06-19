package kroryi.bus2.dto.lowfloorbus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "저상버스 대체 안내 등록 요청 DTO")
public class CreateLowFloorBusRequestDTO {

    @Schema(description = "제목", example = "저상버스 3231호(북구3번 노선) 대체운행 안내")
    @NotBlank
    private String title;

    @Schema(description = "작성자", example = "관리자")
    @NotBlank
    private String author;

    @Schema(description = "내용", example = "금일 저상버스 3231호 차량 정비로 인해 일반 버스로 대체 운행됩니다.")
    @NotBlank
    private String content;
} 