package kroryi.bus2.controller.lost;

import kroryi.bus2.dto.lost.FoundItemAdminResponseDTO;
import kroryi.bus2.dto.lost.FoundItemRequestDTO;
import kroryi.bus2.service.lost.FoundItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/found") // 웹 페이지용 경로
@RequiredArgsConstructor
public class FoundItemPageController {

    private final FoundItemService foundItemService;

    // 습득물 목록 + 상세 보기 + 등록 폼 포함
    @GetMapping
    public String showFoundItems(@RequestParam(required = false) Long selectedId, Model model) {
        List<FoundItemAdminResponseDTO> foundItems = foundItemService.getAllForAdmin();
        model.addAttribute("foundItems", foundItems);

        if (selectedId != null) {
            model.addAttribute("selectedItem", foundItemService.getFoundItemAdminById(selectedId));
        }

        // 등록용 DTO (빈 값으로 넘김)
        model.addAttribute("foundItemForm", new FoundItemRequestDTO());

        return "admin/found-list"; // → templates/admin/found-list.html
    }

    // 습득물 등록
    @PostMapping
    public String registerFoundItem(@ModelAttribute("foundItemForm") FoundItemRequestDTO dto) {
        foundItemService.registerFoundItem(dto);
        return "redirect:/admin/found"; // 등록 후 목록 페이지로 이동
    }

    // 숨김 처리
    @PostMapping("/hide/{id}")
    public String hideFoundItem(@PathVariable Long id) {
        foundItemService.hideFoundItem(id);
        return "redirect:/admin/found";
    }

    // 삭제 처리
    @PostMapping("/delete/{id}")
    public String deleteFoundItem(@PathVariable Long id) {
        foundItemService.deleteFoundItem(id);
        return "redirect:/admin/found";
    }
}

