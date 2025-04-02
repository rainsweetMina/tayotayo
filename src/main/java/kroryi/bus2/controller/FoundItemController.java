package kroryi.bus2.controller;

import kroryi.bus2.dto.lost.FoundItemListResponseDTO;
import kroryi.bus2.dto.lost.FoundItemRequestDTO;
import kroryi.bus2.entity.FoundItem;
import kroryi.bus2.service.FoundItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/found")
@RequiredArgsConstructor
public class FoundItemController {

    private final FoundItemService foundItemService;

    @PostMapping
    public ResponseEntity<FoundItem> reportFoundItem(@RequestBody FoundItemRequestDTO dto) {
        FoundItem saved = foundItemService.saveFoundItem(dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<FoundItemListResponseDTO>> getAllFoundItems() {
        List<FoundItemListResponseDTO> result = foundItemService.getAllFoundItems();
        return ResponseEntity.ok(result);
    }

}

