package kroryi.bus2.controller.lost;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.lost.LostItemAdminResponseDTO;
import kroryi.bus2.dto.lost.LostItemListResponseDTO;
import kroryi.bus2.dto.lost.LostItemRequestDTO;
import kroryi.bus2.dto.lost.LostItemResponseDTO;
import kroryi.bus2.entity.lost.LostItem;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.LostItemRepository;
import kroryi.bus2.repository.jpa.UserRepository;
import kroryi.bus2.service.lost.LostItemService;
import kroryi.bus2.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "분실물-일반", description = "")
@RestController
@RequestMapping("/api/lost")
@RequiredArgsConstructor
public class LostItemController {

    private final LostItemService lostItemService;
    private final UserRepository userRepository;
    private final LostItemRepository lostItemRepository;

    @Hidden
    @GetMapping("/search")
    public List<LostItemResponseDTO> searchLostItems(
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String busCompany,
            @RequestParam(required = false) String busNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return lostItemService.search(itemName, busCompany, busNumber, startDate, endDate);
    }

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
        List<LostItem> items = lostItemRepository.findByDeletedFalseAndVisibleTrue();

        List<LostItemListResponseDTO> response = items.stream()
                .map(LostItemListResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 🔸 단건 조회
    @Operation(summary = "단건 분실물 조회", description = "ID로 분실물 게시글을 단건 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<LostItemResponseDTO> getLostItemById(@PathVariable Long id) {
        LostItemResponseDTO dto = lostItemService.getLostItemById(id);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "분실물 수정", description = "일반회원이 등록한 분실물 정보를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateLostItem(@PathVariable Long id,
                                               @RequestBody LostItemRequestDTO dto) {
        lostItemService.updateLostItem(id, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "분실물 삭제", description = "일반회원이 등록한 분실물을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLostItem(@PathVariable Long id) {
        lostItemService.deleteLostItem(id); // 내부에서는 soft delete
        return ResponseEntity.noContent().build();
    }
    // LostItemController.java

    @Operation(summary = "내가 등록한 분실물 목록 조회")
    @GetMapping
    public ResponseEntity<List<LostItemResponseDTO>> getMyLostItems() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();  // 로그인한 사용자의 userId (username)

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        List<LostItemResponseDTO> myItems = lostItemService.getMyLostItems(user.getId());
        return ResponseEntity.ok(myItems);
    }



}