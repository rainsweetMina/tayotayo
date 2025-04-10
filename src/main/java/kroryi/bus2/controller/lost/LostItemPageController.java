package kroryi.bus2.controller.lost;

import kroryi.bus2.dto.lost.LostItemAdminResponseDTO;
import kroryi.bus2.service.lost.LostItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/lost") // ✅ 이제 api 아님!
@RequiredArgsConstructor
public class LostItemPageController {

    private final LostItemService lostItemService;

    @GetMapping
    public String showAdminLostList(@RequestParam(required = false) Long selectedId, Model model) {
        List<LostItemAdminResponseDTO> lostItems = lostItemService.getAllForAdmin();
        model.addAttribute("lostItems", lostItems);

        if (selectedId != null) {
            LostItemAdminResponseDTO selectedItem = lostItemService.getLostItemAdminById(selectedId);
            model.addAttribute("selectedItem", selectedItem);
        }

        return "admin/lost-list"; // templates/admin/lost-list.html
    }
}
