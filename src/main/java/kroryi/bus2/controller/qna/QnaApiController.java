package kroryi.bus2.controller.qna;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kroryi.bus2.dto.qna.*;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.UserRepository;
import kroryi.bus2.service.QnaAdminService;
import kroryi.bus2.service.QnaService;
import kroryi.bus2.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Tag(name = "질문-관리", description = "")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qna")
public class QnaApiController {

    private final QnaService qnaService;
    private final QnaAdminService qnaAdminService;
    private final UserRepository userRepository;


    @Operation(summary = "QnA 단건 조회", description = "QnA 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<QnaResponseDTO> getQnaDetail(@PathVariable Long id, Authentication auth) {
        Long requesterId = null;
        boolean isAdmin = false;

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String userId = auth.getName();
            User user = userRepository.findByUserId(userId).orElse(null);
            if (user != null) {
                requesterId = user.getId();
            }
            isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

        QnaResponseDTO response = qnaAdminService.getQnaDetail(id, requesterId, isAdmin);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "QnA 전체 조회 (관리자)", description = "관리자용 QnA 전체 목록을 조회합니다.")
    @GetMapping("/admin")
    public ResponseEntity<List<QnaResponseDTO>> getAllQnaForAdmin() {
        List<QnaResponseDTO> qnas = qnaAdminService.getAllQnaForAdmin();
        return ResponseEntity.ok(qnas);
    }

    @Operation(summary = "QnA 숨김 처리 (관리자)", description = "관리자가 QnA를 숨깁니다.")
    @PatchMapping("/admin/hide/{id}")
    public ResponseEntity<String> hideQna(@PathVariable Long id) {
        qnaAdminService.hideQna(id);
        return ResponseEntity.ok("숨김 처리 완료");
    }

    @Operation(summary = "QnA 통계 (관리자)", description = "답변 여부 등을 포함한 QnA 통계를 제공합니다.")
    @GetMapping("/admin/stats")
    public ResponseEntity<QnaStatsDTO> getQnaStats() {
        QnaStatsDTO stats = qnaAdminService.getQnaStatistics();
        return ResponseEntity.ok(stats);
    }


    @Operation(summary = "QnA 삭제 (관리자)", description = "관리자가 QnA를 삭제합니다.")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<String> deleteQna(@PathVariable Long id) {
        qnaAdminService.deleteQna(id); // 관리자 전용 삭제 메서드
        return ResponseEntity.ok("질문글이 삭제되었습니다.");
    }


    @Operation(summary = "QnA 답변 등록 (관리자)", description = "관리자가 QnA에 답변을 등록합니다.")
    @PutMapping("/{id}/answer")
    public ResponseEntity<Void> answerQna(@PathVariable Long id,
                                          @RequestBody QnaAnswerDTO dto) {
        qnaAdminService.answerQna(id, dto.getAnswer());
        return ResponseEntity.ok().build();
    }

    @Hidden
    @Operation(summary = "Q&A 수정", description = "유저 전용 API")
    @PostMapping("/edit/{id}")
    public ResponseEntity<Void> updateQna(
            @PathVariable Long id,
            @RequestBody QnaQuestionRequestDTO dto,
            Authentication authentication) {
        qnaService.updateQuestion(id, dto, authentication);
        return ResponseEntity.ok().build();
    }

    @Hidden
    @Operation(summary = "Q&A 등록", description = "유저 전용 API")
    @PostMapping("/form")
    public ResponseEntity<String> createQuestion(@RequestBody QnaQuestionRequestDTO dto){
        qnaService.createQuestion(dto);
        return ResponseEntity.ok().build();
    }

    @Hidden
    @Operation(summary = "Q&A 조회", description = "유저 전용 API")
    @GetMapping("/list")
    public List<QnaListDTO> getQnaListApi(Authentication authentication) {
        String userId = authentication.getName();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다"));

        return qnaService.getQnaListByUserId(user.getId());
    }

    @Hidden
    @Operation(summary = "Q&A 삭제", description = "유저 전용 API")
    @DeleteMapping("/view/{id}")
    public ResponseEntity<Void> deleteQna(@PathVariable Long id, Authentication authentication) {
        qnaService.deleteQuestion(id, authentication);
        return ResponseEntity.ok().build();
    }

    // 🔒 사용자 전용: 내 질문 목록
    @GetMapping("/mypage")
    public ResponseEntity<List<QnaListDTO>> getUserQnaList(Authentication auth) {
        String userId = auth.getName();
        return ResponseEntity.ok(qnaService.getQnaByUser(userId));
    }

    // 🔒 사용자 전용: 내 질문 작성
    @PostMapping("/mypage")
    public ResponseEntity<Void> createQna(Authentication auth,
                                          @RequestBody @Valid QnaQuestionRequestDTO dto) {
        dto.setUserId(auth.getName());
        qnaService.createQuestion(dto);
        return ResponseEntity.ok().build();
    }

    // 🔒 사용자 전용: 내 미답변 QnA 수
    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getUserQnaPendingCount(Authentication authentication) {
        String userId = authentication.getName();
        int count = qnaService.countUnansweredQnaByUser(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @Operation(summary = "QnA 전체 목록 조회 (일반 회원용, 페이징/검색)", description = "공개된 QnA 전체 목록을 페이징/검색과 함께 조회합니다.")
    @GetMapping("/page")
    public ResponseEntity<Page<QnaListDTO>> getQnaPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "title") String field
    ) {
        return ResponseEntity.ok(qnaService.getQnaPage(keyword, field, page));
    }

}
