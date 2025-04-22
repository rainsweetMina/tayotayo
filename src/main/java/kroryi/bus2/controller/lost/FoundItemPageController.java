package kroryi.bus2.controller.lost;

import jakarta.validation.Valid;
import kroryi.bus2.dto.lost.FoundItemAdminResponseDTO;
import kroryi.bus2.dto.lost.FoundItemRequestDTO;
import kroryi.bus2.entity.lost.Photo;
import kroryi.bus2.repository.jpa.PhotoRepository;
import kroryi.bus2.service.lost.FoundItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin/found")
@RequiredArgsConstructor
@Log4j2
public class FoundItemPageController {

    private final FoundItemService foundItemService;
    private final PhotoRepository photoRepository; // ✅ 추가

    @Value("${file.url-prefix}")
    private String fileUrlPrefix;
//    // ✅ 등록 + 목록 화면
//    @GetMapping
//    public String showFoundItemList(Model model) {
//        model.addAttribute("foundItems", foundItemService.getAllFoundItems());
//        model.addAttribute("foundItemForm", new FoundItemRequestDTO());
//        return "admin/found-list";
//    }

    // ✅ 습득물 등록
    @PostMapping
    public String registerFoundItem(
            @ModelAttribute FoundItemRequestDTO dto,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        foundItemService.registerFoundItem(dto, image);
        return "redirect:/admin/found";
    }

    // ✅ 상세보기 및 수정 폼 페이지
    @GetMapping("/view/{id}")
    public String viewFoundItem(@PathVariable Long id, Model model) {
        FoundItemAdminResponseDTO selectedItem = foundItemService.getFoundItemById(id);
        model.addAttribute("selectedItem", selectedItem);

        // ✅ 여기서 직접 URL을 가져오지 말고, photo 테이블에서 조회
        Photo photo = photoRepository.findByFoundItemId(id); // 반드시 found_item_id로 연결되어 있어야 함

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


        model.addAttribute("foundItemForm", dto);
        model.addAttribute("uploadPrefix", fileUrlPrefix);

        // 날짜 포맷
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = selectedItem.getFoundTime().format(formatter);
        model.addAttribute("formattedDate", formattedDate);

        return "admin/found-detail";
    }


    // ✅ 수정 처리
    @PostMapping("/update/{id}")
    public String updateFoundItem(
            @PathVariable Long id,
            @ModelAttribute FoundItemRequestDTO dto,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        foundItemService.updateFoundItem(id, dto, image);
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
        if (foundItemId == null) {
            throw new IllegalArgumentException("습득물 ID가 비어있습니다.");
        }

        foundItemService.matchFoundItem(foundItemId, lostItemId);
        return "redirect:/admin/found";
    }


}
