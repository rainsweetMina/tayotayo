package kroryi.bus2.dto.qna;

import kroryi.bus2.entity.Qna;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.UserRepository;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaListDTO {
    private Long id;
    private String title;
    private String status;
    private String username;
    private boolean isSecret;
    private LocalDateTime createdAt;

    public static QnaListDTO from(Qna qna, UserRepository userRepository) {
        String username = "Unknown";
        if (qna.getMemberId() != null) {
            username = userRepository.findById(qna.getMemberId())
                    .map(User::getUsername)
                    .orElse("Unknown");
        }

        return QnaListDTO.builder()
                .id(qna.getId())
                .title(qna.getTitle())
                .status(qna.getStatus().getDescription()) // 한글 변환
                .createdAt(qna.getCreatedAt())
                .username(username)
                .isSecret(qna.isSecret())
                .build();
    }
}
