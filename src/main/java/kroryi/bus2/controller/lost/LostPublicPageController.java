package kroryi.bus2.controller.lost;

import kroryi.bus2.dto.lost.FoundItemListResponseDTO;
import kroryi.bus2.dto.lost.LostItemListResponseDTO;
import kroryi.bus2.dto.lost.LostItemRequestDTO;
import kroryi.bus2.dto.lost.LostItemResponseDTO;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.lost.FoundItemService;
import kroryi.bus2.service.lost.LostItemService;
import kroryi.bus2.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LostPublicPageController {

    private final LostItemService lostItemService;
    private final FoundItemService foundItemService;
    private final UserService userService;


    @GetMapping("/bus/lost")
    public String showLostMain(Model model) {
        List<LostItemListResponseDTO> list = lostItemService.getVisibleLostItemsAsDTO();
        model.addAttribute("lostList", list);
        return "lost/lostMain";
    }

    @GetMapping("/bus/lost/detail/{id}")
    public String showLostDetail(@PathVariable Long id, Model model) {
        LostItemResponseDTO item = lostItemService.getLostItemById(id); // 숨김/삭제 체크 포함
        model.addAttribute("item", item);
        return "lost/lostDetail";
    }

    @GetMapping("/bus/lost/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("lostItem", new LostItemRequestDTO());
        return "lost/lostRegister";
    }

    @PostMapping("/bus/lost/register")
    public String registerLostItem(@ModelAttribute LostItemRequestDTO dto) {
        // 현재 로그인한 사용자 ID 추출
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        User user = userService.findByUserId(userId);  // 실제 User 객체 불러오기
        if (user == null) {
            throw new RuntimeException("로그인 사용자 정보를 찾을 수 없습니다.");
        }

        dto.setReporterId(user.getId()); // reporterId 세팅
        lostItemService.saveLostItem(dto);

        return "redirect:/bus/lost";  // 목록으로 리다이렉트
    }


    @GetMapping("/bus/found")
    public String showFoundItems(Model model) {
        List<FoundItemListResponseDTO> items = foundItemService.getFoundItemsForPublic();
        model.addAttribute("foundItems", items);
        return "lost/foundMain";
    }


}
