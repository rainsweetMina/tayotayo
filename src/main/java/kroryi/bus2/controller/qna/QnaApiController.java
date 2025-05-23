package kroryi.bus2.controller.qna;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kroryi.bus2.dto.qna.*;
import kroryi.bus2.service.QnaAdminService;
import kroryi.bus2.service.QnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "질문-관리", description = "")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qna")
public class QnaApiController {

    private final QnaService qnaService;
    private final QnaAdminService qnaAdminService;

    @Operation(summary = "QnA 등록", description = "사용자가 새 질문을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "질문 등록 완료")
    })
    @PostMapping
    public ResponseEntity<Long> createQna(@RequestBody @Valid QnaRequestDTO requestDTO) {
        Long qnaId = qnaAdminService.createQna(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(qnaId);
    }

    @Operation(summary = "QnA 전체 조회", description = "일반 사용자용 QnA 전체 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<QnaResponseDTO>> getAllQna() {
        List<QnaResponseDTO> qnaList = qnaAdminService.getAllVisibleQna();
        return ResponseEntity.ok(qnaList);
    }

    @Operation(summary = "QnA 단건 조회", description = "QnA 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<QnaResponseDTO> getQnaDetail(@PathVariable Long id) {
        Long memberId = 1L; // ✅ 실제 로그인 사용자 ID로 대체
        boolean isAdmin = false; // ✅ 관리자 여부 판단 로직 필요

        QnaResponseDTO response = qnaAdminService.getQnaDetail(id, memberId, isAdmin);
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

    @Operation(summary = "QnA 수정", description = "작성자가 본인의 QnA를 수정합니다.")
    @PatchMapping("/{id}")
    public ResponseEntity<String> updateQna(@PathVariable Long id,
                                            @RequestBody QnaUpdateDTO dto) {
        Long memberId = dto.getMemberId();
        qnaAdminService.updateQna(id, memberId, dto);
        return ResponseEntity.ok("질문글이 수정되었습니다.");
    }

    @Operation(summary = "QnA 삭제", description = "작성자가 본인의 QnA를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQna(@PathVariable Long id) {
        Long memberId = 1L; // ✅ 실제 로그인 사용자 ID로 대체해야 함 (임시값)
        qnaAdminService.deleteQna(id, memberId);
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
    public List<QnaListDTO> getQnaListApi() {
        return qnaService.getAllQna();
    }

    @Hidden
    @Operation(summary = "Q&A 삭제", description = "유저 전용 API")
    @DeleteMapping("/view/{id}")
    public ResponseEntity<Void> deleteQna(@PathVariable Long id, Authentication authentication) {
        qnaService.deleteQuestion(id, authentication);
        return ResponseEntity.ok().build();
    }

}
