package kroryi.bus2.service;

import jakarta.persistence.EntityNotFoundException;
import kroryi.bus2.dto.qna.QnaListDTO;
import kroryi.bus2.dto.qna.QnaQuestionRequestDTO;
import kroryi.bus2.dto.qna.QnaRequestDTO;
import kroryi.bus2.dto.qna.QnaResponseDTO;
import kroryi.bus2.entity.Qna;
import kroryi.bus2.entity.QnaStatus;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.QnaRepository;
import kroryi.bus2.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QnaQuestionService {

    private final QnaRepository qnaRepository;
    private final UserRepository userRepository;

    // 해당 멤버만 등록
    public void createQuestion(QnaQuestionRequestDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        User user = userRepository.findByUserId(userId).orElseThrow();

        Qna qna = Qna.builder()
                .memberId(user.getId())
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

    // 리스트 조회
    @Transactional(readOnly = true)
    public List<QnaListDTO> getAllQna() {
        return qnaRepository.findAll().stream()
                .filter(q -> !q.isDeleted())
                .map(q -> {
                    String username = userRepository.findById(q.getMemberId())
                            .map(User::getUsername)
                            .orElse("Unknown");
                    return new QnaListDTO(
                            q.getId(),
                            q.getTitle(),
                            q.getStatus().name(),
                            username,
                            q.isSecret(),
                            q.getCreatedAt()
                    );
                }).collect(Collectors.toList());
    }

    // 수정
    @Transactional
    public void updateQuestion(Long id, QnaQuestionRequestDTO dto, Authentication authentication) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("QnA not found"));

        String currentUserId = authentication.getName();
        User user = userRepository.findByUserId(currentUserId).orElseThrow();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !user.getId().equals(qna.getMemberId())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        qna.setTitle(dto.getTitle());
        qna.setContent(dto.getContent());
        qna.setSecret(dto.isSecret());
        qna.setUpdatedAt(LocalDateTime.now());

        qnaRepository.save(qna);
    }
    
    // 삭제
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

        qnaRepository.delete(qna);  // 💥 실제 삭제
    }

}

