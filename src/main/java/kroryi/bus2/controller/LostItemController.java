package kroryi.bus2.controller;


import kroryi.bus2.dto.lost.LostItemListResponseDTO;
import kroryi.bus2.dto.lost.LostItemRequestDTO;
import kroryi.bus2.entity.LostItem;
import kroryi.bus2.service.LostItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lost")
@RequiredArgsConstructor
public class LostItemController {

    private final LostItemService lostItemService;

    @PostMapping
    public ResponseEntity<LostItem> reportLostItem(@RequestBody LostItemRequestDTO dto) {
        LostItem saved = lostItemService.saveLostItem(dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<LostItemListResponseDTO>> getAllLostItems() {
        List<LostItemListResponseDTO> result = lostItemService.getAllLostItems();
        return ResponseEntity.ok(result);
    }
}
