package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.Qna;
import kroryi.bus2.entity.QnaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QnaRepository extends JpaRepository<Qna, Long> {

    // 사용자용: 삭제되지 않았고, 관리자에 의해 숨겨지지 않은 Q&A만 조회
    List<Qna> findByIsDeletedFalseAndVisibleTrueOrderByCreatedAtDesc();

    // 관리자용: 전체 조회 (숨김 여부 무시)
    List<Qna> findAllByOrderByCreatedAtDesc();

    // 특정 회원의 Q&A 목록
    List<Qna> findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(Long memberId);

    // 조건 기반 단건 조회 (예: 비공개 필터링에 활용)
    Optional<Qna> findByIdAndIsDeletedFalse(Long id);

    // 미답변 QnA 개수
    long countByStatus(QnaStatus status);

    // 오늘 등록된 QnA 수
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 비공개 QnA 수
    long countByIsSecretTrue();

    // 전체 QnA 수
    long count();

    // 숨김 처리된 QnA 수
    long countByVisibleFalse();

}

