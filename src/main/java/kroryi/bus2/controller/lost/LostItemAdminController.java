package kroryi.bus2.controller.lost;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.lost.LostItemAdminResponseDTO;
import kroryi.bus2.dto.lost.LostItemListResponseDTO;
import kroryi.bus2.dto.lost.LostItemStatsDTO;
import kroryi.bus2.service.lost.LostItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "분실물-관리", description = "")
@RestController
@RequestMapping("/api/admin/lost")
@RequiredArgsConstructor
public class LostItemAdminController {

    private final LostItemService lostItemService;

    @Operation(summary = "전체 분실물 관리자용 조회", description = "숨김 여부와 상관없이 모든 분실물을 관리자 페이지에서 조회합니다.")
    @GetMapping
    public ResponseEntity<List<LostItemAdminResponseDTO>> getAllLostItemsForAdmin() {
        List<LostItemAdminResponseDTO> list = lostItemService.getAllForAdmin();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "분실물 숨김 처리", description = "해당 분실물 게시글을 숨김 처리합니다.")
    @PatchMapping("/hide/{id}")
    public ResponseEntity<String> hideLostItem(@PathVariable Long id) {
        lostItemService.hideLostItem(id);
        return ResponseEntity.ok("숨김 처리 완료");
    }

    @Operation(summary = "분실물 삭제 처리", description = "해당 분실물 게시글을 소프트 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLostItem(@PathVariable Long id) {
        lostItemService.deleteLostItem(id);
        return ResponseEntity.ok("삭제 처리 완료");
    }

    @Operation(summary = "분실물 상세 조회 (관리자용)", description = "관리자가 상세 페이지에서 해당 분실물 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<LostItemAdminResponseDTO> getLostItemAdminById(@PathVariable Long id) {
        LostItemAdminResponseDTO dto = lostItemService.getLostItemAdminById(id);
        return ResponseEntity.ok(dto);
    }

    // ✅ 통계 조회: 분실물, 습득물, 매칭 건수 및 미매칭 건수
    @Operation(summary = "분실물 통계 조회", description = "분실물, 습득물, 매칭 수 및 미매칭 건수를 통계로 반환합니다.")
    @GetMapping("/stats")
    public LostItemStatsDTO getLostItemStatistics() {
        return lostItemService.getLostItemStatistics();
    }

    @Operation(summary = "전체 분실물 조회 (모든 상태)", description = "관리자/운영자가 분실물 전체를 조회할 때 사용합니다.")
    @GetMapping("/all")
    public ResponseEntity<List<LostItemListResponseDTO>> getAllLostItemsIncludingHidden() {
        List<LostItemListResponseDTO> result = lostItemService.getAllLostItemsIncludingHidden()
                .stream()
                .map(item -> LostItemListResponseDTO.builder()
                        .id(item.getId())
                        .title(item.getTitle())
                        .busNumber(item.getBusNumber())
                        .lostTime(item.getLostTime())
                        .createdAt(item.getCreatedAt())
                        .updatedAt(item.getUpdatedAt())
                        .build())
                .toList();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "전체 분실물 조회 (관리자용 포함)", description = "숨김/삭제 포함 전체 분실물 목록을 조회합니다.")
    @GetMapping("/legacy-all")
    public ResponseEntity<List<LostItemListResponseDTO>> getAllLostItemsLegacy() {
        List<LostItemListResponseDTO> results = lostItemService.getAllLostItems();
        return ResponseEntity.ok(results);
    }
}
