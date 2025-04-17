package kroryi.bus2.controller.lost;

import kroryi.bus2.dto.lost.FoundItemResponseDTO;
import kroryi.bus2.service.lost.FoundItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/found")
@RequiredArgsConstructor
public class FoundItemController {

    private final FoundItemService foundItemService;

    // ✅ 전체 조회
    @GetMapping
    public List<FoundItemResponseDTO> getAllVisibleFoundItems() {
        return foundItemService.getVisibleFoundItemsForUser();
    }

    // ✅ 단건 조회
    @GetMapping("/{id}")
    public FoundItemResponseDTO getFoundItemById(@PathVariable Long id) {
        return foundItemService.getFoundItemDetailForUser(id);
    }
}
