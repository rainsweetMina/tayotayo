package kroryi.bus2.service;

import jakarta.persistence.EntityNotFoundException;
import kroryi.bus2.dto.qna.QnaListDTO;
import kroryi.bus2.dto.qna.QnaQuestionRequestDTO;
import kroryi.bus2.entity.Qna;
import kroryi.bus2.entity.QnaStatus;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.QnaRepository;
import kroryi.bus2.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaRepository qnaRepository;
    private final UserRepository userRepository;

    public void createQuestion(QnaQuestionRequestDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        User user = userRepository.findByUserId(userId).orElseThrow();

        Qna qna = Qna.builder()
                .memberId(user.getId()) // ✅ 수정된 부분
                .title(dto.getTitle())
                .content(dto.getContent())
                .status(QnaStatus.WAITING)
                .isSecret(dto.isSecret())
                .isDeleted(false)
                .visible(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .answer(null)
                .build();

        qnaRepository.save(qna);
    }

    @Transactional(readOnly = true)
    public List<QnaListDTO> getAllQna() {
        return qnaRepository.findAll().stream()
                .filter(q -> !q.isDeleted() && q.isVisible())
                .map(q -> QnaListDTO.from(q, userRepository)) // ✅ 수정된 부분
                .collect(Collectors.toList());
    }

    public Page<QnaListDTO> getQnaPage(String keyword, String field, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Qna> qnaEntities;
        if (!StringUtils.hasText(keyword)) {
            qnaEntities = qnaRepository.findByIsDeletedFalse(pageable);
        } else {
            switch (field) {
                case "title":
                    qnaEntities = qnaRepository.findByTitleContainingAndIsDeletedFalse(keyword, pageable);
                    break;
                case "user":
                    List<Long> userIds = userRepository.findByUsernameContaining(keyword)
                            .stream().map(User::getId).toList();
                    if (userIds.isEmpty()) {
                        return Page.empty(pageable);
                    }
                    qnaEntities = qnaRepository.findByMemberIdInAndIsDeletedFalse(userIds, pageable);
                    break;
                default:
                    qnaEntities = qnaRepository.findByIsDeletedFalseAndTitleContainingOrContentContaining(keyword, keyword, pageable);
            }
        }

        return qnaEntities.map(q -> QnaListDTO.from(q, userRepository)); // ✅ 수정
    }

    @Transactional
    public void updateQuestion(Long id, QnaQuestionRequestDTO dto, Authentication authentication) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("QnA not found"));

        String currentUserId = authentication.getName();
        User user = userRepository.findByUserId(currentUserId).orElseThrow();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !user.getId().equals(qna.getMemberId())) { // ✅ 수정
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        qna.setTitle(dto.getTitle());
        qna.setContent(dto.getContent());
        qna.setSecret(dto.isSecret());
        qna.setUpdatedAt(LocalDateTime.now());

        qnaRepository.save(qna);
    }

    @Transactional
    public void deleteQuestion(Long id, Authentication authentication) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("QnA not found"));

        String userId = authentication.getName();
        User user = userRepository.findByUserId(userId).orElseThrow();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !user.getId().equals(qna.getMemberId())) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        qnaRepository.delete(qna);  // 💥 실제 삭제 방식
    }

    public int countUnansweredQnaByUser(String userId) {
        Long memberId = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."))
                .getId();

        return qnaRepository.countByMemberIdAndStatus(memberId, QnaStatus.WAITING);
    }

    @Transactional(readOnly = true)
    public List<QnaListDTO> getQnaByUser(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow();
        List<Qna> qnas = qnaRepository.findByMemberIdAndIsDeletedFalseAndVisibleTrueOrderByCreatedAtDesc(user.getId());

        return qnas.stream()
                .map(q -> QnaListDTO.from(q, userRepository))
                .collect(Collectors.toList());
    }

    public List<QnaListDTO> getQnaListByUserId(Long userId) {
        return qnaRepository.findByMemberIdAndIsDeletedFalseAndVisibleTrueOrderByCreatedAtDesc(userId).stream()
                .map(q -> QnaListDTO.from(q, userRepository))
                .collect(Collectors.toList());
    }
}
