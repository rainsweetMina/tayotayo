package kroryi.bus2.controller.lost;

import kroryi.bus2.dto.lost.LostItemAdminResponseDTO;
import kroryi.bus2.dto.lost.LostItemListResponseDTO;
import kroryi.bus2.dto.lost.LostItemResponseDTO;
import kroryi.bus2.dto.lost.LostItemStatsDTO;
import kroryi.bus2.service.lost.LostItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/lost")
@RequiredArgsConstructor
public class LostItemAdminController {

    private final LostItemService lostItemService;

    @GetMapping
    public ResponseEntity<List<LostItemAdminResponseDTO>> getAllLostItemsForAdmin() {
        List<LostItemAdminResponseDTO> list = lostItemService.getAllForAdmin();
        return ResponseEntity.ok(list);
    }
    @PatchMapping("/hide/{id}")
    public ResponseEntity<String> hideLostItem(@PathVariable Long id) {
        lostItemService.hideLostItem(id);
        return ResponseEntity.ok("숨김 처리 완료");
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLostItem(@PathVariable Long id) {
        lostItemService.deleteLostItem(id);
        return ResponseEntity.ok("삭제 처리 완료");
    }
    @GetMapping("/{id}")
    public ResponseEntity<LostItemAdminResponseDTO> getLostItemAdminById(@PathVariable Long id) {
        LostItemAdminResponseDTO dto = lostItemService.getLostItemAdminById(id);
        return ResponseEntity.ok(dto);
    }
    // ✅ 통계 조회: 분실물, 습득물, 매칭 건수 및 미매칭 건수
    @GetMapping("/stats")
    public LostItemStatsDTO getLostItemStatistics() {
        return lostItemService.getLostItemStatistics();
    }


}
