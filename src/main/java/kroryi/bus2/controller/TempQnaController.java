package kroryi.bus2.controller;

import kroryi.bus2.dto.qna.QnaListDTO;
import kroryi.bus2.dto.qna.QnaQuestionRequestDTO;
import kroryi.bus2.entity.Qna;
import kroryi.bus2.entity.QnaStatus;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.QnaRepository;
import kroryi.bus2.repository.jpa.UserRepository;
import kroryi.bus2.service.QnaQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class TempQnaController {
    private final QnaQuestionService qnaQuestionService;
    private final QnaRepository qnaRepository;
    private final UserRepository userRepository;

    // Q&A 리스트 페이지
    @GetMapping("/qna/list")
    public String showQnaList(Model model) {
        List<QnaListDTO> qnaList = qnaQuestionService.getAllQna().stream()
                .sorted(Comparator.comparing(QnaListDTO::getCreatedAt).reversed())
                .collect(Collectors.toList());
        model.addAttribute("qnaList", qnaList);
        return "/qna/qnaList"; // 타임리프 템플릿 경로
    }

    // 질문 페이지
    @GetMapping("/qna/question")
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

    // 수정 페이지 API
    @PostMapping("/api/qna-edit/{id}")
    @ResponseBody
    public ResponseEntity<Void> updateQna(
            @PathVariable Long id,
            @RequestBody QnaQuestionRequestDTO dto,
            Authentication authentication) {
        qnaQuestionService.updateQuestion(id, dto, authentication);
        return ResponseEntity.ok().build();
    }

    // 질문 등록 API
    @PostMapping("/api/qna-form")
    @ResponseBody
    public ResponseEntity<String> createQuestion(@RequestBody QnaQuestionRequestDTO dto){
        qnaQuestionService.createQuestion(dto);
        return ResponseEntity.ok().build();
    }

    // Q&A 조회 API
    @GetMapping("/api/qna-list")
    @ResponseBody
    public List<QnaListDTO> getQnaListApi() {
        return qnaQuestionService.getAllQna();
    }

    @DeleteMapping("/api/qna-view/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteQna(@PathVariable Long id, Authentication authentication) {
        qnaQuestionService.deleteQuestion(id, authentication);
        return ResponseEntity.ok().build();
    }

}
