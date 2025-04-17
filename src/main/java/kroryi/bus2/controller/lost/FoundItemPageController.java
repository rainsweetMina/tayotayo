package kroryi.bus2.controller.lost;

import jakarta.validation.Valid;
import kroryi.bus2.dto.lost.FoundItemAdminResponseDTO;
import kroryi.bus2.dto.lost.FoundItemRequestDTO;
import kroryi.bus2.service.lost.FoundItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin/found")
@RequiredArgsConstructor
public class FoundItemPageController {

    private final FoundItemService foundItemService;

//    // ✅ 등록 + 목록 화면
//    @GetMapping
//    public String showFoundItemList(Model model) {
//        model.addAttribute("foundItems", foundItemService.getAllFoundItems());
//        model.addAttribute("foundItemForm", new FoundItemRequestDTO());
//        return "admin/found-list";
//    }

    // ✅ 습득물 등록
    @PostMapping
    public String registerFoundItem(@Valid @ModelAttribute("foundItemForm") FoundItemRequestDTO requestDTO,
                                    BindingResult bindingResult,
                                    Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("foundItems", foundItemService.getAllFoundItems());
            return "admin/found-list";
        }
        foundItemService.registerFoundItem(requestDTO);
        return "redirect:/admin/found";
    }

    // ✅ 상세보기 및 수정 폼 페이지
    @GetMapping("/view/{id}")
    public String viewFoundItem(@PathVariable Long id, Model model) {
        FoundItemAdminResponseDTO selectedItem = foundItemService.getFoundItemById(id);
        model.addAttribute("selectedItem", selectedItem);

        // ✅ 여기에 이 코드 추가!
        FoundItemRequestDTO dto = new FoundItemRequestDTO();
        dto.setItemName(selectedItem.getItemName());
        dto.setBusCompany(selectedItem.getBusCompany());
        dto.setBusNumber(selectedItem.getBusNumber());
        dto.setFoundPlace(selectedItem.getFoundPlace());
        dto.setContent(selectedItem.getContent());
        dto.setHandlerContact(selectedItem.getHandlerContact());
        dto.setHandlerEmail(selectedItem.getHandlerEmail());
        dto.setStatus(selectedItem.getStatus());
        dto.setStorageLocation(selectedItem.getStorageLocation());
        dto.setFoundTime(selectedItem.getFoundTime());
        dto.setHandlerId(selectedItem.getHandlerId());
        dto.setPhotoUrl(selectedItem.getPhotoUrl());
        model.addAttribute("foundItemForm", dto);

// ✅ 날짜 포맷해서 넘기기 (yyyy-MM-dd)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = selectedItem.getFoundTime().format(formatter);
        model.addAttribute("formattedDate", formattedDate);


        return "admin/found-detail";
    }

    // ✅ 수정 처리
    @PostMapping("/update/{id}")
    public String updateFoundItem(@PathVariable Long id,
                                  @Valid @ModelAttribute("foundItemForm") FoundItemRequestDTO dto,
                                  BindingResult bindingResult,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            FoundItemAdminResponseDTO selectedItem = foundItemService.getFoundItemById(id);
            model.addAttribute("selectedItem", selectedItem);
            return "admin/found-detail";
        }

        foundItemService.updateFoundItem(id, dto);
        return "redirect:/admin/found/view/" + id;
    }

    // ✅ 숨김
    @PostMapping("/hide/{id}")
    public String hideFoundItem(@PathVariable Long id) {
        foundItemService.hideFoundItem(id);
        return "redirect:/admin/found";
    }

    // ✅ 삭제
    @PostMapping("/delete/{id}")
    public String deleteFoundItem(@PathVariable Long id) {
        foundItemService.deleteFoundItem(id);
        return "redirect:/admin/found";
    }

    // ✅ 매칭
    @PostMapping("/match/{foundItemId}") // ✅ 여기 수정
    public String matchWithLostItem(
            @PathVariable Long foundItemId, // ✅ 경로와 일치
            @RequestParam(required = false) Long lostItemId) {
        if (foundItemId == null || lostItemId == null) {
            throw new IllegalArgumentException("ID 값이 비어있습니다.");
        }

        foundItemService.matchFoundItem(foundItemId, lostItemId);
        return "redirect:/admin/found";
    }


}
