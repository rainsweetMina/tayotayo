package kroryi.bus2.controller;

import jakarta.validation.Valid;
import kroryi.bus2.dto.qna.QnaAnswerDTO;
import kroryi.bus2.dto.qna.QnaRequestDTO;
import kroryi.bus2.dto.qna.QnaResponseDTO;
import kroryi.bus2.dto.qna.QnaStatsDTO;
import kroryi.bus2.service.QnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qna")
public class QnaController {

    private final QnaService qnaService;

    // ✅ Q&A 등록
    @PostMapping
    public ResponseEntity<Long> createQna(@RequestBody @Valid QnaRequestDTO requestDTO) {
        Long qnaId = qnaService.createQna(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(qnaId);
    }

    // ✅ 전체 Q&A 조회 (isDeleted = false, visible = true)
    @GetMapping
    public ResponseEntity<List<QnaResponseDTO>> getAllQna() {
        List<QnaResponseDTO> qnaList = qnaService.getAllVisibleQna();
        return ResponseEntity.ok(qnaList);
    }

    // ✅ 단건 Q&A 조회
    @GetMapping("/{id}")
    public ResponseEntity<QnaResponseDTO> getQnaDetail(@PathVariable Long id) {
        QnaResponseDTO response = qnaService.getQnaDetail(id);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/admin/answer")
    public ResponseEntity<String> answerQna(@RequestBody @Valid QnaAnswerDTO dto) {
        qnaService.answerQna(dto);
        return ResponseEntity.ok("답변이 등록되었습니다.");
    }
    @GetMapping("/admin")
    public ResponseEntity<List<QnaResponseDTO>> getAllQnaForAdmin() {
        List<QnaResponseDTO> qnas = qnaService.getAllQnaForAdmin();
        return ResponseEntity.ok(qnas);
    }
    @PatchMapping("/admin/hide/{id}")
    public ResponseEntity<String> hideQna(@PathVariable Long id) {
        qnaService.hideQna(id);
        return ResponseEntity.ok("숨김 처리 완료");
    }
    @GetMapping("/admin/stats")
    public ResponseEntity<QnaStatsDTO> getQnaStats() {
        QnaStatsDTO stats = qnaService.getQnaStatistics();
        return ResponseEntity.ok(stats);
    }




}
