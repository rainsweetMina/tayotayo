package kroryi.bus2.controller.lost;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.lost.LostItemAdminResponseDTO;
import kroryi.bus2.dto.lost.LostItemListResponseDTO;
import kroryi.bus2.dto.lost.LostItemRequestDTO;
import kroryi.bus2.dto.lost.LostItemResponseDTO;
import kroryi.bus2.entity.lost.LostItem;
import kroryi.bus2.service.lost.LostItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "분실물-일반", description = "")
@RestController
@RequestMapping("/api/lost")
@RequiredArgsConstructor
public class LostItemController {

    private final LostItemService lostItemService;

    @Operation(summary = "분실물 등록", description = "일반회원이 분실물을 등록합니다.")
    @PostMapping
    public ResponseEntity<LostItem> reportLostItem(@RequestBody LostItemRequestDTO dto) {
        LostItem saved = lostItemService.saveLostItem(dto);
        return ResponseEntity.ok(saved);
    }

    // 🔸 일반 회원용 (숨겨지지 않은 것만 조회)
    @Operation(summary = "전체 분실물 조회 (노출용)", description = "일반회원이 볼 수 있도록 숨김/삭제 제외한 분실물 목록을 조회합니다.")
    @GetMapping("/visible")
    public ResponseEntity<List<LostItemListResponseDTO>> getVisibleLostItems() {
        List<LostItemListResponseDTO> result = lostItemService.getAllLostItemsVisibleOnly()
                .stream()
                .map(item -> LostItemListResponseDTO.builder()
                        .id(item.getId())
                        .title(item.getTitle())
                        .busNumber(item.getBusNumber())
                        .lostTime(item.getLostTime())
                        .build())
                .toList();
        return ResponseEntity.ok(result);
    }

    // 🔸 단건 조회
    @Operation(summary = "단건 분실물 조회", description = "ID로 분실물 게시글을 단건 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<LostItemResponseDTO> getLostItemById(@PathVariable Long id) {
        LostItemResponseDTO dto = lostItemService.getLostItemById(id);
        return ResponseEntity.ok(dto);
    }
}