package kroryi.bus2.controller.lost;

import kroryi.bus2.dto.lost.FoundItemResponseDTO;
import kroryi.bus2.service.lost.FoundItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "습득물-일반", description = "")
@RestController
@RequestMapping("/api/found")
@RequiredArgsConstructor
public class FoundItemController {

    private final FoundItemService foundItemService;

    @Operation(summary = "습득물 전체 조회", description = "일반 회원이 열람 가능한 습득물 리스트를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<FoundItemResponseDTO>> getFoundItemsForUser() {
        return ResponseEntity.ok(foundItemService.getVisibleFoundItemsForUser());
    }

    @Operation(summary = "습득물 단건 조회", description = "일반 회원이 열람 가능한 습득물 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<FoundItemResponseDTO> getFoundItemDetailForUser(@PathVariable Long id) {
        return ResponseEntity.ok(foundItemService.getFoundItemDetailForUser(id));
    }
}
