package kroryi.bus2.service;

import jakarta.persistence.EntityNotFoundException;
import kroryi.bus2.aop.AdminAudit;
import kroryi.bus2.dto.qna.*;
import kroryi.bus2.entity.Qna;
import kroryi.bus2.entity.QnaStatus;
import kroryi.bus2.repository.jpa.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QnaAdminService {

    private final QnaRepository qnaRepository;


    // 단건 조회 (권한 체크는 컨트롤러 또는 서비스 확장 시 구현)
    public QnaResponseDTO getQnaDetail(Long qnaId, Long requesterId, boolean isAdmin) {
        Qna qna = qnaRepository.findByIdAndIsDeletedFalse(qnaId)
                .orElseThrow(() -> new EntityNotFoundException("Q&A not found"));

        // 비공개인데 본인이 아니고 관리자도 아닐 경우
        if (qna.isSecret() && !qna.getMemberId().equals(requesterId) && !isAdmin) {
            throw new AccessDeniedException("비공개 글은 작성자 본인만 열람할 수 있습니다.");
        }
        return toResponseDTO(qna);
    }

    // Entity → DTO 변환
    private QnaResponseDTO toResponseDTO(Qna qna) {
        return QnaResponseDTO.builder()
                .id(qna.getId())
                .memberId(qna.getMemberId())
                .title(qna.getTitle())
                .content(qna.getContent())
                .status(qna.getStatus())
                .answer(qna.getAnswer())
                .isSecret(qna.isSecret())
                .isDeleted(qna.isDeleted())
                .visible(qna.isVisible())
                .createdAt(qna.getCreatedAt())
                .updatedAt(qna.getUpdatedAt())
                .build();
    }
    @Transactional
    @AdminAudit(action = "QnA 답변 등록", target = "Qna")
    public void answerQna(Long id, String answer) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 QnA를 찾을 수 없습니다."));
        qna.setAnswer(answer); // ✅ 답변 저장
        qna.setStatus(QnaStatus.ANSWERED); // ✅ 상태변경

        qnaRepository.save(qna);
    }


    public List<QnaResponseDTO> getAllQnaForAdmin() {
        List<Qna> qnas = qnaRepository.findAllByOrderByCreatedAtDesc();
        return qnas.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    @AdminAudit(action = "QnA 숨김 처리", target = "Qna")
    public void hideQna(Long qnaId) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new EntityNotFoundException("Q&A not found"));

        qna.setVisible(false);
    }
    public QnaStatsDTO getQnaStatistics() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusNanos(1);

        return QnaStatsDTO.builder()
                .totalCount(qnaRepository.count())
                .waitingCount(qnaRepository.countByStatus(QnaStatus.WAITING))
                .todayCount(qnaRepository.countByCreatedAtBetween(startOfDay, endOfDay))
                .hiddenCount(qnaRepository.countByVisibleFalse())
                .secretCount(qnaRepository.countByIsSecretTrue())
                .build();
    }

    @Transactional
    @AdminAudit(action = "QnA 삭제", target = "Qna")
    public void deleteQna(Long qnaId) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new EntityNotFoundException("Q&A not found"));
        qna.setDeleted(true);
    }


}

