package kroryi.bus2.controller.lost;

import jakarta.validation.Valid;
import kroryi.bus2.dto.lost.FoundItemAdminResponseDTO;
import kroryi.bus2.dto.lost.FoundItemRequestDTO;
import kroryi.bus2.service.lost.FoundItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "습득물-관리", description = "")
@RestController
@RequestMapping("/api/admin/found")
@RequiredArgsConstructor
public class FoundItemAdminController {

    private final FoundItemService foundItemService;

    @Operation(summary = "습득물 등록", description = "관리자가 습득물 정보를 등록합니다. 이미지 파일 포함 가능")
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<String> registerFoundItem(
            @RequestPart("dto") FoundItemRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        foundItemService.registerFoundItem(dto, image);
        return ResponseEntity.ok("습득물 등록 완료");
    }

    @Operation(summary = "습득물 수정", description = "ID를 기준으로 습득물 정보를 수정합니다.")
    @PostMapping("/update/{id}")
    public ResponseEntity<String> updateFoundItem(
            @PathVariable Long id,
            @ModelAttribute FoundItemRequestDTO dto,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        foundItemService.updateFoundItem(id, dto, image);
        return ResponseEntity.ok("습득물 수정 완료");
    }

    @Operation(summary = "습득물 숨김 처리", description = "ID 기준으로 습득물을 숨깁니다.")
    @PatchMapping("/hide/{id}")
    public ResponseEntity<String> hideFoundItem(@PathVariable Long id) {
        foundItemService.hideFoundItem(id);
        return ResponseEntity.ok("숨김 처리 완료");
    }

    @Operation(summary = "습득물 삭제 처리", description = "ID 기준으로 습득물을 삭제합니다.")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFoundItem(@PathVariable Long id) {
        foundItemService.deleteFoundItem(id);
        return ResponseEntity.ok("삭제 완료");
    }

    @Operation(summary = "습득물 단건 조회", description = "ID 기준으로 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<FoundItemAdminResponseDTO> getFoundItemById(@PathVariable Long id) {
        return ResponseEntity.ok(foundItemService.getFoundItemById(id));
    }

    @Operation(summary = "습득물 전체 조회", description = "관리자용 전체 습득물 리스트를 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<List<FoundItemAdminResponseDTO>> getAllFoundItems() {
        return ResponseEntity.ok(foundItemService.getAllFoundItems());
    }

    @Operation(summary = "습득물-분실물 매칭 처리", description = "습득물 기준으로 매칭을 수행합니다. 분실물 ID는 선택 사항이며, 없을 경우 회수 완료 처리됩니다.")
    @PatchMapping("/match/{foundItemId}")
    public ResponseEntity<String> matchWithLostItem(
            @PathVariable Long foundItemId,
            @RequestParam(required = false) Long lostItemId) {
        foundItemService.matchFoundItem(foundItemId, lostItemId);
        return ResponseEntity.ok("매칭 완료");
    }

    @Operation(summary = "관리자용 습득물 목록/검색", description = "키워드 없으면 전체, 있으면 통합검색")
    @GetMapping
    public ResponseEntity<List<FoundItemAdminResponseDTO>> getAllOrSearchFoundItemsForAdmin(
            @RequestParam(value = "keyword", required = false) String keyword) {
        List<FoundItemAdminResponseDTO> list;
        if (keyword == null || keyword.isBlank()) {
            list = foundItemService.getAllFoundItems();
        } else {
            list = foundItemService.searchByKeywordForAdmin(keyword);
        }
        return ResponseEntity.ok(list);
    }



}
