package kroryi.bus2.controller.qna;

import kroryi.bus2.dto.qna.QnaListDTO;
import kroryi.bus2.entity.Qna;
import kroryi.bus2.entity.QnaStatus;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.QnaRepository;
import kroryi.bus2.repository.jpa.UserRepository;
import kroryi.bus2.service.QnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class QnaController {
    private final QnaService qnaService;
    private final QnaRepository qnaRepository;
    private final UserRepository userRepository;

    // Q&A 리스트 페이지
    @GetMapping("/qna/list")
    public String showQnaList(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false, defaultValue = "title")String field,
                              Model model) {
        Page<QnaListDTO> qnaPage = qnaService.getQnaPage(keyword, field, page); // 서비스 수정 필요
        model.addAttribute("qnaPage", qnaPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("field", field);
        return "/qna/qnaList";
    }

    // 질문 페이지
    @GetMapping("/qna/form")
    public String showQuestion() {
        return "/qna/qnaForm";
    }

    // 뷰 페이지
    @GetMapping("/qna/view/{id}")
    public String getQnaDetail(@PathVariable Long id, Model model, Authentication authentication) {
        Qna qna = qnaRepository.findById(id).orElseThrow();
        boolean isAuthor = false;
        boolean isAdmin = false;

        // 작성자 / 관리자 여부 판단
        if (authentication != null && authentication.isAuthenticated()) {
            String currentUserId = authentication.getName();
            User user = userRepository.findByUserId(currentUserId).orElse(null);

            if (user != null) {
                isAuthor = user.getId().equals(qna.getMemberId());
            }

            isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

        // 비공개일 경우 접근 제한 처리
        if (qna.isSecret() && !isAuthor && !isAdmin) {
            model.addAttribute("popupMessage", "비공개 글입니다. 열람 권한이 없습니다.");
            return "/qna/qnaPopupError";
        }

        model.addAttribute("qna", qna);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isAuthor", isAuthor);
        return "/qna/qnaView";
    }

    // 수정페이지
    @GetMapping("/qna/edit/{id}")
    public String editQnaForm(@PathVariable Long id, Model model, Authentication authentication) {
        Qna qna = qnaRepository.findById(id).orElseThrow();

        String userId = authentication.getName();
        User user = userRepository.findByUserId(userId).orElseThrow();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !user.getId().equals(qna.getMemberId())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        model.addAttribute("qna", qna);
        return "/qna/qnaEdit";
    }

    // 관리자 답변
    @PostMapping("/qna/answer/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveAnswer(@PathVariable Long id, @RequestParam String answer) {
        Qna qna = qnaRepository.findById(id).orElseThrow();
        qna.setAnswer(answer);
        qna.setStatus(QnaStatus.ANSWERED);
        qnaRepository.save(qna);
        return "redirect:/qna/view/" + id;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mypage/qna")
    public String showMyQnaList(Model model, Authentication auth) {
        String userId = auth.getName();
        User user = userRepository.findByUserId(userId).orElseThrow();
        List<Qna> myQna = qnaRepository.findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(user.getId());

        model.addAttribute("myQnaList", myQna);
        return "/qna/myQnaList";
    }


}
